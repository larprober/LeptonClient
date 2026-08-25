package dev.lepton.event;

import dev.lepton.Lepton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflective event bus. Subscribers expose methods annotated with {@link EventHandler}
 * taking exactly one parameter -- the event type they want.
 */
public class EventBus {
    private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

    private record Listener(Object target, Method method, int priority) {}

    public void subscribe(Object target) {
        for (Method method : resolveMethods(target.getClass())) {
            Class<?> eventType = method.getParameterTypes()[0];
            int priority = method.getAnnotation(EventHandler.class).priority();

            List<Listener> list = listeners.computeIfAbsent(eventType, k -> new ArrayList<>());
            synchronized (list) {
                for (Listener existing : list) {
                    if (existing.target == target && existing.method.equals(method)) return;
                }
                list.add(new Listener(target, method, priority));
                list.sort(Comparator.comparingInt(Listener::priority).reversed());
            }
        }
    }

    public void unsubscribe(Object target) {
        for (List<Listener> list : listeners.values()) {
            synchronized (list) {
                list.removeIf(l -> l.target == target);
            }
        }
    }

    public <T> T post(T event) {
        List<Listener> list = listeners.get(event.getClass());
        if (list == null) return event;

        List<Listener> snapshot;
        synchronized (list) {
            snapshot = new ArrayList<>(list);
        }

        for (Listener listener : snapshot) {
            try {
                listener.method.invoke(listener.target, event);
            } catch (IllegalAccessException e) {
                Lepton.LOG.error("Event handler not accessible: {}", listener.method, e);
            } catch (InvocationTargetException e) {
                Lepton.LOG.error("Event handler {} threw", listener.method, e.getCause());
            }
        }

        return event;
    }

    private List<Method> resolveMethods(Class<?> type) {
        return methodCache.computeIfAbsent(type, t -> {
            List<Method> found = new ArrayList<>();

            for (Class<?> c = t; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(EventHandler.class)) continue;

                    if (method.getParameterCount() != 1) {
                        throw new IllegalStateException(
                            "@EventHandler method " + c.getName() + "." + method.getName() + " must take exactly one parameter");
                    }

                    method.setAccessible(true);
                    found.add(method);
                }
            }

            return found;
        });
    }
}
