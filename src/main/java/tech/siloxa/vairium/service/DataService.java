package tech.siloxa.vairium.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.siloxa.vairium.domain.User;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataService extends AbstractSchemaService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserService userService;

    @Transactional
    public Object create(final String schemaName, final Map<String, Object> payload) throws JsonProcessingException {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Payload must not be empty");
        }

        final User currentUser = userService.getUserWithAuthorities().orElseThrow();

        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        final String columns = payload.keySet().stream()
            .map(this::sanitize)
            .collect(Collectors.joining(", "));

        final String placeholders = payload.keySet().stream()
            .map(key -> "?")
            .collect(Collectors.joining(", "));

        final String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

        final Object[] values = payload.keySet().stream()
            .map(payload::get)
            .toArray();

        jdbcTemplate.update(sql, values);

        return payload;
    }

    public Map<String, Object> read(final String schemaName, final String id) {
        final User currentUser = userService.getUserWithAuthorities().orElseThrow();

        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        final String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);

        return jdbcTemplate.queryForMap(sql, Long.valueOf(id));
    }

    public List<Map<String, Object>> readAll(final String schemaName) {
        final User currentUser = userService.getUserWithAuthorities().orElseThrow();

        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        final String sql = String.format("SELECT * FROM %s", tableName);

        return jdbcTemplate.queryForList(sql);
    }
}
