package tech.siloxa.vairium.domain.enumeration;

/**
 * The DataType enumeration.
 */
public enum DataType {

    STRING("TEXT"),
    INTEGER("INTEGER"),
    LONG("BIGINT"),
    FLOAT("REAL"),
    DOUBLE("DOUBLE PRECISION"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE");

    private final String postgresType;

    DataType(String postgresType) {
        this.postgresType = postgresType;
    }

    public String getPostgresType() {
        return postgresType;
    }
}
