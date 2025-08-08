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
import tech.siloxa.vairium.domain.FieldDefinition;
import tech.siloxa.vairium.repository.FieldDefinitionRepository;

/**
 * Spring Data Elasticsearch repository for the {@link FieldDefinition} entity.
 */
public interface FieldDefinitionSearchRepository
    extends ElasticsearchRepository<FieldDefinition, Long>, FieldDefinitionSearchRepositoryInternal {}

interface FieldDefinitionSearchRepositoryInternal {
    Stream<FieldDefinition> search(String query);

    Stream<FieldDefinition> search(Query query);

    void index(FieldDefinition entity);
}

class FieldDefinitionSearchRepositoryInternalImpl implements FieldDefinitionSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final FieldDefinitionRepository repository;

    FieldDefinitionSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, FieldDefinitionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<FieldDefinition> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery);
    }

    @Override
    public Stream<FieldDefinition> search(Query query) {
        return elasticsearchTemplate.search(query, FieldDefinition.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(FieldDefinition entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
