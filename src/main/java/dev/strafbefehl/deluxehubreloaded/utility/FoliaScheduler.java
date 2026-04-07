package dev.strafbefehl.deluxehubreloaded.utility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class FoliaScheduler {

    private static final boolean FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");

    private FoliaScheduler() {
    }

    public static TaskHandle run(Plugin plugin, Runnable runnable) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTask(plugin, runnable));
        }

        Object globalScheduler = invokeStatic(Bukkit.class, "getGlobalRegionScheduler");
        Object scheduled = invoke(globalScheduler, "run", plugin, (Consumer<Object>) ignored -> runnable.run());
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runLater(Plugin plugin, Runnable runnable, long delayTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
        }

        Object globalScheduler = invokeStatic(Bukkit.class, "getGlobalRegionScheduler");
        Object scheduled = invoke(globalScheduler, "runDelayed", plugin, (Consumer<Object>) ignored -> runnable.run(),
                delayTicks);
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runTimer(Plugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks));
        }

        long safeInitialDelay = normalizeFixedRateInitialDelay(initialDelayTicks);
        long safePeriod = normalizeFixedRatePeriod(periodTicks);

        Object globalScheduler = invokeStatic(Bukkit.class, "getGlobalRegionScheduler");
        Object scheduled = invoke(globalScheduler, "runAtFixedRate", plugin,
                (Consumer<Object>) ignored -> runnable.run(), safeInitialDelay, safePeriod);
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runAsync(Plugin plugin, Runnable runnable) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
        }

        Object asyncScheduler = invokeStatic(Bukkit.class, "getAsyncScheduler");
        Object scheduled = invoke(asyncScheduler, "runNow", plugin, (Consumer<Object>) ignored -> runnable.run());
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runTimerAsync(Plugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(
                    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelayTicks, periodTicks));
        }

        long initialDelayMillis = ticksToMillis(initialDelayTicks);
        long periodMillis = ticksToMillis(periodTicks);
        Object asyncScheduler = invokeStatic(Bukkit.class, "getAsyncScheduler");
        Object scheduled = invoke(asyncScheduler, "runAtFixedRate", plugin,
                (Consumer<Object>) ignored -> runnable.run(), initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runAtEntity(Entity entity, Plugin plugin, Runnable runnable) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTask(plugin, runnable));
        }

        Object entityScheduler = invoke(entity, "getScheduler");
        Object scheduled = invoke(entityScheduler, "run", plugin, (Consumer<Object>) ignored -> runnable.run(), null);
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runLaterAtEntity(Entity entity, Plugin plugin, Runnable runnable, long delayTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
        }

        Object entityScheduler = invoke(entity, "getScheduler");
        Object scheduled = invoke(entityScheduler, "runDelayed", plugin, (Consumer<Object>) ignored -> runnable.run(),
                null, delayTicks);
        return new TaskHandle(scheduled);
    }

    public static TaskHandle runTimerAtEntity(Entity entity, Plugin plugin, Runnable runnable, long initialDelayTicks,
            long periodTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");

        if (!FOLIA) {
            return new TaskHandle(Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks));
        }

        long safeInitialDelay = normalizeFixedRateInitialDelay(initialDelayTicks);
        long safePeriod = normalizeFixedRatePeriod(periodTicks);

        Object entityScheduler = invoke(entity, "getScheduler");
        Object scheduled = invoke(entityScheduler, "runAtFixedRate", plugin,
                (Consumer<Object>) ignored -> runnable.run(), null, safeInitialDelay, safePeriod);
        return new TaskHandle(scheduled);
    }

    public static void cancelTasks(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (!FOLIA) {
            Bukkit.getScheduler().cancelTasks(plugin);
            return;
        }

        try {
            Object globalScheduler = invokeStatic(Bukkit.class, "getGlobalRegionScheduler");
            invoke(globalScheduler, "cancelTasks", plugin);
        } catch (RuntimeException ignored) {
        }

        try {
            Object asyncScheduler = invokeStatic(Bukkit.class, "getAsyncScheduler");
            invoke(asyncScheduler, "cancelTasks", plugin);
        } catch (RuntimeException ignored) {
        }
    }

    public static final class TaskHandle {
        private final Object task;

        private TaskHandle(Object task) {
            this.task = task;
        }

        public void cancel() {
            if (task == null) {
                return;
            }

            if (task instanceof BukkitTask) {
                ((BukkitTask) task).cancel();
                return;
            }

            invoke(task, "cancel");
        }

        public boolean isPresent() {
            return task != null;
        }
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(1L, ticks) * 50L;
    }

    private static long normalizeFixedRateInitialDelay(long ticks) {
        return Math.max(1L, ticks);
    }

    private static long normalizeFixedRatePeriod(long ticks) {
        return Math.max(1L, ticks);
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static Object invokeStatic(Class<?> type, String methodName, Object... args) {
        Method method = findMethod(type, methodName, args);
        try {
            return method.invoke(null, args);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Failed to invoke static method " + methodName + " on " + type.getName(), ex);
        }
    }

    private static Object invoke(Object target, String methodName, Object... args) {
        if (target == null) {
            throw new IllegalStateException("Cannot call " + methodName + " on null target");
        }
        Method method = findMethod(target.getClass(), methodName, args);
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on " + target.getClass().getName(),
                    ex);
        }
    }

    private static Method findMethod(Class<?> type, String methodName, Object... args) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != args.length) {
                continue;
            }
            if (!areArgumentsCompatible(parameterTypes, args)) {
                continue;
            }
            method.setAccessible(true);
            return method;
        }
        throw new IllegalStateException("Could not find method " + methodName + " on " + type.getName());
    }

    private static boolean areArgumentsCompatible(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            Class<?> parameter = parameterTypes[i];
            if (arg == null) {
                if (parameter.isPrimitive()) {
                    return false;
                }
                continue;
            }
            Class<?> boxed = box(parameter);
            if (!boxed.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class)
            return Integer.class;
        if (type == long.class)
            return Long.class;
        if (type == boolean.class)
            return Boolean.class;
        if (type == byte.class)
            return Byte.class;
        if (type == short.class)
            return Short.class;
        if (type == float.class)
            return Float.class;
        if (type == double.class)
            return Double.class;
        if (type == char.class)
            return Character.class;
        return Void.class;
    }
}
