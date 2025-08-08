package tech.siloxa.vairoxa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.siloxa.vairoxa.domain.User;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DataService extends AbstractSchemaService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserService userService;

    @Transactional
    public Map<String, Object> create(final String schemaName, final Map<String, Object> payload) throws JsonProcessingException {
        final User currentUser = userService.getUserWithAuthorities().orElseThrow();
        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        final String columns = payload.keySet().stream()
            .map(this::sanitize)
            .collect(Collectors.joining(", "));

        final String placeholders = payload.keySet().stream()
            .map(key -> "?")
            .collect(Collectors.joining(", "));

        final String sql = String.format("INSERT INTO %s (%s) VALUES (%s) RETURNING id", tableName, columns, placeholders);

        final Object[] values = payload.keySet().stream()
            .map(payload::get)
            .toArray();

        final Long generatedId = jdbcTemplate.queryForObject(sql, Long.class, values);

        payload.put("id", generatedId);

        return payload;
    }

    public Map<String, Object> read(final String schemaName, final String id) {
        try {
            final User currentUser = userService.getUserWithAuthorities().orElseThrow();
            final String tableName = sanitize(getSchemaName(schemaName, currentUser));

            final String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);

            return jdbcTemplate.queryForMap(sql, Long.valueOf(id));
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public List<Map<String, Object>> readAll(final String schemaName) {
        final User currentUser = userService.getUserWithAuthorities().orElseThrow();
        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        final String sql = String.format("SELECT * FROM %s", tableName);

        return jdbcTemplate.queryForList(sql);
    }

    @Transactional
    public Pair<Integer, Map<String, Object>> update(final String schemaName, final String id, final Map<String, Object> payload) {
        final User currentUser = userService.getUserWithAuthorities().orElseThrow();
        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        payload.remove("id");

        final String setClause = payload.keySet().stream()
            .map(this::sanitize)
            .map(col -> col + " = ?")
            .collect(Collectors.joining(", "));

        final String sql = String.format("UPDATE %s SET %s WHERE id = ?", tableName, setClause);

        final Object[] values = Stream.concat(
            payload.keySet().stream().map(payload::get),
            Stream.of(Long.valueOf(id))
        ).toArray();

        final int updated = jdbcTemplate.update(sql, values);

        payload.put("id", id);

        return Pair.of(updated, payload);
    }

    @Transactional
    public int delete(final String schemaName, final String id) {
        final User currentUser = userService.getUserWithAuthorities().orElseThrow();
        final String tableName = sanitize(getSchemaName(schemaName, currentUser));

        final String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);

        return jdbcTemplate.update(sql, Long.valueOf(id));
    }
}
