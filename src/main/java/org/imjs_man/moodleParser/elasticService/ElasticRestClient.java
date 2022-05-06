package org.imjs_man.moodleParser.elasticService;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.imjs_man.moodleParser.repository.CourseRepository;
import org.imjs_man.moodleParser.service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertEquals;

@Component
@RunWith( SpringRunner.class )
@SpringBootTest
@Transactional
public class ElasticRestClient {

    @Autowired
    CourseService courseService;

    public void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {


    }
    public void allCourses() throws Exception {
        ArrayList<CourseEntity> courseEntities = courseService.getAllCourses();
        System.out.println(courseEntities.size());
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.create();

        BulkRequest request = new BulkRequest();
        for (CourseEntity courseEntity:courseEntities)
        {
            request.add(new IndexRequest(CourseEntity.class.getSimpleName().toLowerCase()).id(String.valueOf(courseEntity.getId())).source(gson.toJson(courseEntity),XContentType.JSON));
        }
        RestHighLevelClient client = createSimpleElasticClient();
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkResponse) {
                System.out.println(bulkResponse);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
            }
        };
        BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulkResponse);
    }
//    @Test
    public void givenJsonString_whenJavaObject_thenIndexDocument() throws Exception {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
//        Student student = gson.fromJson(jsonString, Student.class);
//        System.out.println(student);
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setCourseimage("asdasd");
        courseEntity.setFullnamedisplay("FullName");
        courseEntity.setCoursecategory("OFK");
        courseEntity.setName("JustName");
        courseEntity.setId(123124);


        String jsonString = gson.toJson(courseEntity);
        System.out.println(jsonString);



        IndexRequest request = new IndexRequest(courseEntity.getClass().getSimpleName().toLowerCase());
        request.source(jsonString, XContentType.JSON);
        RestHighLevelClient client = createSimpleElasticClient();
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        String index = response.getIndex();
        long version = response.getVersion();

        assertEquals(DocWriteResponse.Result.CREATED, response.getResult());
        assertEquals(1, version);
        assertEquals("people", index);
    }

    @Test
    public void testDiffQuery() throws Exception {
        ArrayList<String> queries = new ArrayList<>();
        queries.add("English course");
        queries.add("Всероссийский");
        queries.add("Английский");
        queries.add("(СО)");
        queries.add("знание");
        queries.add("теория процессов");
        // todo add id in entity expory



        for (String query:queries)
        {
            System.out.println(query);
            FindInElastic(query);
        }
    }
    public void FindInElastic(String query) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        RestHighLevelClient client = createSimpleElasticClient();
        SearchRequest searchRequest = new SearchRequest(CourseEntity.class.getSimpleName().toLowerCase());  // index where searching
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); // all parameters there
        sourceBuilder.query(QueryBuilders.queryStringQuery(query)); // _all mean all fields
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit: searchHits)
        {
            CourseEntity course = gson.fromJson(hit.getSourceAsString(), CourseEntity.class);
            System.out.println(course.getId()+ " "+ course.getName() );
        }
    }


    public RestHighLevelClient createSimpleElasticClient() throws Exception {
        try {
            final CredentialsProvider credentialsProvider =
                    new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials("elastic","asdfghjk"));

            SSLContextBuilder sslBuilder = SSLContexts.custom()
                    .loadTrustMaterial(null, (x509Certificates, s) -> true);
            final SSLContext sslContext = sslBuilder.build();
            RestHighLevelClient client = new RestHighLevelClient(RestClient
                    .builder(new HttpHost("5.187.3.41", 9200, "http"))
//                    .builder(new HttpHost("185.46.10.89", 9200, "http"))
                    .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                        @Override
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                            return httpClientBuilder
                                    .setSSLContext(sslContext)
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                    .setDefaultCredentialsProvider(credentialsProvider);
                        }
                    })
                    .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                        @Override
                        public RequestConfig.Builder customizeRequestConfig(
                                RequestConfig.Builder requestConfigBuilder) {
                            return requestConfigBuilder.setConnectTimeout(5000)
                                    .setSocketTimeout(120000);
                        }
                    }));
            System.out.println("elasticsearch client created");
            return client;
        } catch (Exception e) {
            System.out.println(e);
            throw new Exception("Could not create an elasticsearch client!!");
        }
    }
}