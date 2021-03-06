package org.atomium.metadata;

import com.google.common.reflect.TypeToken;

/**
 * @author Blackrush
 */
public class SimpleColumnInfo implements ColumnInfo {
    private final String name;
    private final TypeToken<?> target;
    private final boolean primaryKey, autogenerated, nullable;

    private SimpleColumnInfo(String name, TypeToken<?> target, boolean primaryKey, boolean autogenerated, boolean nullable) {
        this.name = name;
        this.target = target;
        this.primaryKey = primaryKey;
        this.autogenerated = autogenerated;
        this.nullable = nullable;
    }

    public static SimpleColumnInfo create(String name, TypeToken<?> target, boolean primaryKey, boolean autogenerated, boolean nullable) {
        return new SimpleColumnInfo(name, target, primaryKey, autogenerated, nullable);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleColumnInfo withName(String name) {
        return create(name, target, primaryKey, autogenerated, nullable);
    }

    @Override
    public TypeToken<?> getTarget() {
        return target;
    }

    @Override
    public SimpleColumnInfo withTarget(TypeToken<?> target) {
        return create(name, target, primaryKey, autogenerated, nullable);
    }

    @Override
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public SimpleColumnInfo withPrimaryKey(boolean primaryKey) {
        return create(name, target, primaryKey, autogenerated, nullable);
    }

    @Override
    public boolean isAutogenerated() {
        return autogenerated;
    }

    @Override
    public SimpleColumnInfo withAutogenerated(boolean autogenerated) {
        return create(name, target, primaryKey, autogenerated, nullable);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public SimpleColumnInfo withNullable(boolean nullable) {
        return create(name, target, primaryKey, autogenerated, nullable);
    }

    @Override
    public ColumnInfo asColumnInfo() {
        return create(name, target, primaryKey, autogenerated, nullable);
    }
}
