package org.afterlike.openutils.util.lang;

import java.lang.reflect.Field;

public final class ReflectionUtil {
	private ReflectionUtil() {
	}

	@SafeVarargs
	public static <Return, Owner> Return getField(final Owner object, final String fieldName,
			final Owner... mock) {
		try {
			final Class<?> clazz = object == null
					? mock.getClass().getComponentType()
					: object.getClass();
			if (clazz == null) {
				throw new ClassNotFoundException();
			}
			Field field;
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (final NoSuchFieldException e) {
				try {
					field = clazz.getField(fieldName);
				} catch (final NoSuchFieldException ex) {
					field = null;
				}
			}
			if (field == null) {
				throw new NoSuchFieldException();
			}
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			// noinspection unchecked
			return (Return) field.get(object);
		} catch (final Exception caught) {
			throw new RuntimeException("Failed to get field: " + fieldName, caught);
		}
	}

	@SafeVarargs
	public static <Value, Owner> void setField(final Owner object, final String fieldName,
			final Value value, final Owner... mock) {
		try {
			final Class<?> clazz = object == null
					? mock.getClass().getComponentType()
					: object.getClass();
			if (clazz == null) {
				throw new ClassNotFoundException();
			}
			Field field;
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (final NoSuchFieldException e) {
				try {
					field = clazz.getField(fieldName);
				} catch (final NoSuchFieldException ex) {
					field = null;
				}
			}
			if (field == null) {
				throw new NoSuchFieldException();
			}
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(object, value);
		} catch (final Exception caught) {
			throw new RuntimeException("Failed to set field: " + fieldName, caught);
		}
	}
}
