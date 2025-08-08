package tech.siloxa.vairoxa.service;

import tech.siloxa.vairoxa.domain.User;

public abstract class AbstractSchemaService {

    protected String getSchemaName(String schemaName, User currentUser) {
        return schemaName + "_" + currentUser.getId();
    }

    protected String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
    }
}
