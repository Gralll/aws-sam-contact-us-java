package com.gralll.sam;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(App.class)
public class AppTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private App app;

    @Mock
    private Context context;
    @Mock
    private LambdaLogger lambdaLogger;
    @Mock
    private AwsClientFactory awsClientFactory;
    @Mock
    private AmazonSimpleEmailService amazonSimpleEmailService;
    @Mock
    private DynamoDB dynamoDB;
    @Mock
    private Table table;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        whenNew(AwsClientFactory.class).withNoArguments().thenReturn(awsClientFactory);
        given(context.getLogger()).willReturn(lambdaLogger);
        given(awsClientFactory.getSesClient()).willReturn(amazonSimpleEmailService);
        given(awsClientFactory.getDynamoDB()).willReturn(dynamoDB);
        given(amazonSimpleEmailService.sendEmail(any(SendEmailRequest.class))).willReturn(new SendEmailResult().withMessageId("123"));
        given(dynamoDB.getTable(anyString())).willReturn(table);
        given(table.putItem(any(Item.class))).willReturn(null);
        app = new App();
    }

    @Test
    public void shouldReturnSuccessfulResponse() throws IOException {
        // given
        AwsProxyRequest awsProxyRequest =
                OBJECT_MAPPER.readValue(
                        this.getClass().getClassLoader().getResourceAsStream("contact_us_request.json"),
                        AwsProxyRequest.class);

        // when
        AwsProxyResponse awsProxyResponse = app.handleRequest(awsProxyRequest, context);

        // then
        assertNotNull(awsProxyResponse);
        assertThat(awsProxyResponse.getStatusCode(), is(200));
    }

    @Test
    public void shouldOnlyWarmUpLambda() throws IOException {
        // given
        AwsProxyRequest awsProxyRequest =
                OBJECT_MAPPER.readValue(
                        this.getClass().getClassLoader().getResourceAsStream("warm_up_request.json"),
                        AwsProxyRequest.class);

        // when
        AwsProxyResponse awsProxyResponse = app.handleRequest(awsProxyRequest, context);

        // then
        assertNotNull(awsProxyResponse);
        assertThat(awsProxyResponse.getStatusCode(), is(201));
        assertThat(awsProxyResponse.getBody(), is("{\"response\": \"Lambda was warmed up. V5\"}"));
    }
}