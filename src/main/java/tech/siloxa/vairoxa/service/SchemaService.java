package tech.siloxa.vairoxa.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.violations.ConstraintViolationProblem;
import tech.siloxa.vairoxa.domain.FieldDefinition;
import tech.siloxa.vairoxa.domain.SchemaDefinition;
import tech.siloxa.vairoxa.domain.User;
import tech.siloxa.vairoxa.repository.SchemaDefinitionRepository;

import javax.annotation.Resource;

@Service
public class SchemaService extends AbstractSchemaService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private SchemaDefinitionRepository schemaDefinitionRepository;

    @Resource
    private UserService userService;

    @Transactional
    public SchemaDefinition create(final SchemaDefinition schemaDefinition)  {
        try {
            final User currentUser = userService.getUserWithAuthorities().orElseThrow();
            schemaDefinition.setUser(currentUser);
            schemaDefinitionRepository.save(schemaDefinition);

            final String createTableSql = buildCreateTableSql(schemaDefinition, currentUser);
            jdbcTemplate.execute(createTableSql);

            return schemaDefinition;
        }  catch (ConstraintViolationProblem ex) {
            return null;
        }
    }

    private String buildCreateTableSql(final SchemaDefinition schemaDefinition, final User user) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("CREATE TABLE IF NOT EXISTS ")
            .append(sanitize(getSchemaName(schemaDefinition.getName(), user)))
            .append(" (\n");

        stringBuilder.append("id SERIAL PRIMARY KEY,\n");

        for (FieldDefinition field : schemaDefinition.getFieldDefinitions()) {
            stringBuilder.append("  ").append(sanitize(field.getName())).append(" ")
                .append(field.getDataType().getPostgresType());

            if (field.getIsUnique()) {
                stringBuilder.append(" UNIQUE");
            }
            if (field.getIsRequired()) {
                stringBuilder.append(" NOT NULL");
            }
            stringBuilder.append(",\n");
        }

        int lastComma = stringBuilder.lastIndexOf(",");
        if (lastComma != -1) {
            stringBuilder.deleteCharAt(lastComma);
        }

        stringBuilder.append(");");

        return stringBuilder.toString();
    }
}
