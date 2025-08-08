package tech.siloxa.vairium.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.stream.Stream;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.siloxa.vairium.domain.SchemaDefinition;
import tech.siloxa.vairium.repository.SchemaDefinitionRepository;

/**
 * Spring Data Elasticsearch repository for the {@link SchemaDefinition} entity.
 */
public interface SchemaDefinitionSearchRepository
    extends ElasticsearchRepository<SchemaDefinition, Long>, SchemaDefinitionSearchRepositoryInternal {}

interface SchemaDefinitionSearchRepositoryInternal {
    Stream<SchemaDefinition> search(String query);

    Stream<SchemaDefinition> search(Query query);

    void index(SchemaDefinition entity);
}

class SchemaDefinitionSearchRepositoryInternalImpl implements SchemaDefinitionSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final SchemaDefinitionRepository repository;

    SchemaDefinitionSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, SchemaDefinitionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<SchemaDefinition> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<SchemaDefinition> search(Query query) {
        return elasticsearchTemplate.search(query, SchemaDefinition.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(SchemaDefinition entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
