package org.imjs_man.moodleParser.elasticService;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.imjs_man.moodleParser.entity.supporting.SuperEntity;
import org.imjs_man.moodleParser.service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.testng.AssertJUnit.assertEquals;

@Service
@RunWith( SpringRunner.class )
@SpringBootTest
@Transactional
public class ElasticRestClientService {

    @Autowired
    CourseService courseService;

    @Autowired
    RestHighLevelClient simpleElasticClient;

    @Autowired
    Gson gsonExpose;

    private ActionListener<BulkResponse> getDefaultListener()
    {
        return new ActionListener<>() {
            @Override
            public void onResponse(BulkResponse bulkResponse) {
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
    }

    public void indexObjectAsync(ArrayList<SuperEntity> items)
    {
        indexObjectAsync(items, getDefaultListener());
    }
    public void indexObjectAsync(ArrayList<SuperEntity> items, ActionListener<BulkResponse> actionListener)
    {
        BulkRequest request = new BulkRequest();
        for(SuperEntity item:items) {

            request.add(new IndexRequest(CourseEntity.class.getSimpleName().toLowerCase())
                    .id(String.valueOf(item.getId()))
                    .source(gsonExpose.toJson(item), XContentType.JSON));
        }
        simpleElasticClient.bulkAsync(request, RequestOptions.DEFAULT, actionListener);
    }

    public void indexObjectAsync(SuperEntity item)
    {
        BulkRequest request = new BulkRequest();
        request.add(new IndexRequest(CourseEntity.class.getSimpleName().toLowerCase())
                    .id(String.valueOf(item.getId()))
                    .source(gsonExpose.toJson(item), XContentType.JSON));
        simpleElasticClient.bulkAsync(request, RequestOptions.DEFAULT, getDefaultListener());
    }

    public Cancellable findByQueryAsync(String index,String query, ActionListener<SearchResponse> actionListener) throws Exception {
        return findByQueryAsync(index, query, false, actionListener);
    }
    public Cancellable findByQueryAsync(String index,String query, boolean fuzzyQuery, ActionListener<SearchResponse> actionListener) throws Exception {
        SearchRequest searchRequest = new SearchRequest(index);             // index where searching
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();      // all parameters there
        if (fuzzyQuery)
            sourceBuilder.query(QueryBuilders.queryStringQuery(query+"~")); // ~ mean fuzzy search
        else
            sourceBuilder.query(QueryBuilders.queryStringQuery(query));
        searchRequest.source(sourceBuilder);
        return simpleElasticClient.searchAsync(searchRequest, RequestOptions.DEFAULT, actionListener);  // it may be canceled by
    }


    @Test
    public void allCourses() throws Exception {
        ArrayList<CourseEntity> courseEntities = courseService.getAllCourses();
               BulkRequest request = new BulkRequest();
        for (CourseEntity courseEntity:courseEntities)
        {
            request.add(new IndexRequest(CourseEntity.class.getSimpleName().toLowerCase())
                    .id(String.valueOf(courseEntity.getId()))
                    .source(gsonExpose.toJson(courseEntity),XContentType.JSON));
        }
        BulkResponse bulkResponse = simpleElasticClient.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulkResponse);
        Thread.sleep(1000);
    }

    @Test
    public void testDiffQuery() throws Exception {
        ArrayList<String> queries = new ArrayList<>();
        queries.add("English course~");
        queries.add("Английский");
        queries.add("Всероссийский");
        queries.add("(СО)");
        queries.add("знание");
        queries.add("теория процессов");
        queries.add("терия процессоs~");
        queries.add("Всеросийский");
        queries.add("Всеросийский~");

        ActionListener<SearchResponse> actionListener = new ActionListener<>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                for (SearchHit hit: searchResponse.getHits())
                {
                    CourseEntity course = gsonExpose.fromJson(hit.getSourceAsString(), CourseEntity.class);
                    System.out.println(hit.getId()+ " "+ course.getName() );
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Some error occurred  " +e.getMessage());
            }
        };
        for (String query:queries)
        {
            System.out.println(query);
            long startTime = System.currentTimeMillis();
            Cancellable c = findByQueryAsync(CourseEntity.class.getSimpleName().toLowerCase(), query, actionListener);
            //          System.out.println("------------- Time: "+(System.currentTimeMillis() - startTime)+"--------------");
            Thread.sleep(1500);
        }
    }



}