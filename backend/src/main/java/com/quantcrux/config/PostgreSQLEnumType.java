package com.quantcrux.config;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

public class PostgreSQLEnumType implements UserType<Enum> {

    private Class<Enum> enumClass;

    public PostgreSQLEnumType() {}

    public PostgreSQLEnumType(Class<Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<Enum> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Enum x, Enum y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Enum x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Enum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String columnValue = rs.getString(position);
        if (columnValue == null) {
            return null;
        }
        return Enum.valueOf(enumClass, columnValue);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.toString(), Types.OTHER);
        }
    }

    @Override
    public Enum deepCopy(Enum value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Enum value) throws HibernateException {
        return value;
    }

    @Override
    public Enum assemble(Serializable cached, Object owner) throws HibernateException {
        return (Enum) cached;
    }
}