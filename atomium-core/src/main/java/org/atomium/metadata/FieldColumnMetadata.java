package org.atomium.metadata;

import com.google.common.reflect.TypeToken;
import org.atomium.annotations.Column;
import org.atomium.annotations.PrimaryKey;

import java.lang.reflect.Field;

import static com.google.common.base.Throwables.propagate;

/**
 * @author Blackrush
 */
public class FieldColumnMetadata<T> extends ColumnMetadata<T> {
    private final Field field;

    private boolean primaryKey, autogenerated, nullable;

    public FieldColumnMetadata(Metadata<T> parent, String name, Field field) {
        super(parent, name);
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public Object get(T instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw propagate(e);
        }
    }

    @Override
    public void set(T instance, Object o) {
        try {
            field.set(instance, o);
        } catch (IllegalAccessException e) {
            throw propagate(e);
        }
    }

    @Override
    public void load() {
        nullable = field.getAnnotation(Column.class).nullable();

        PrimaryKey pkey = field.getAnnotation(PrimaryKey.class);
        if (pkey != null) {
            primaryKey = true;
            autogenerated = pkey.autogenerated();
            nullable = false;
        }
    }

    @Override
    public TypeToken<?> getTarget() {
        return TypeToken.of(field.getGenericType());
    }

    @Override
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public boolean isAutogenerated() {
        return autogenerated;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public ColumnInfo withName(String name) {
        return SimpleColumnInfo.create(name, getTarget(), primaryKey, autogenerated, nullable);
    }

    @Override
    public ColumnInfo withTarget(TypeToken<?> target) {
        return SimpleColumnInfo.create(getName(), target, primaryKey, autogenerated, nullable);
    }

    @Override
    public ColumnInfo withPrimaryKey(boolean primaryKey) {
        return SimpleColumnInfo.create(getName(), getTarget(), primaryKey, autogenerated, nullable);
    }

    @Override
    public ColumnInfo withAutogenerated(boolean autogenerated) {
        return SimpleColumnInfo.create(getName(), getTarget(), primaryKey, autogenerated, nullable);
    }

    @Override
    public ColumnInfo withNullable(boolean nullable) {
        return SimpleColumnInfo.create(getName(), getTarget(), primaryKey, autogenerated, nullable);
    }
}
