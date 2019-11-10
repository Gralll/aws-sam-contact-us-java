package com.gralll.sam;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gralll.sam.model.ContactUsRequest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

public class App implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    private static final String SENDER_EMAIL = "aleksandrgruzdev11@gmail.com";
    private static final String RECIPIENT_EMAIL = "aleksandrgruzdev11@gmail.com";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final AwsClientFactory awsClientFactory = new AwsClientFactory();

    /**
     * Send ContactUs form data to a specific email
     * and put the form data into database.
     *
     * The handler doesn't have correct error handling.
     *
     * @param request API Gateway request
     * @param context Lambda context
     * @return Proxy response to API Gateway
     */
    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Request was received");
        logger.log(getAsString(request));

        // WARMING UP
        if (request.getMultiValueHeaders().containsKey("X-WARM-UP")) {
            logger.log("Lambda was warmed up");
            return buildResponse(201, "Lambda was warmed up. V4");
        }

        ContactUsRequest contactUsRequest = getContactUsRequest(request);

        SendEmailResult sendEmailResult = sendEmail(logger, contactUsRequest);
        logger.log("Email was sent");

        addEmailDetailsToDb(contactUsRequest, sendEmailResult);
        logger.log("DB is updated");

        return buildResponse(200,
                String.format("Message %s has been sent successfully.", sendEmailResult.getMessageId()));
    }

    private String getAsString(AwsProxyRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private AwsProxyResponse buildResponse(int statusCode, String body) {
        AwsProxyResponse awsProxyResponse = new AwsProxyResponse();
        awsProxyResponse.setBody(String.format("{\"response\": \"%s\"}", body));
        awsProxyResponse.addHeader("Access-Control-Allow-Origin", "*");
        //awsProxyResponse.addHeader("Content-Type", "application/json");
        awsProxyResponse.setStatusCode(statusCode);
        return awsProxyResponse;
    }

    private void addEmailDetailsToDb(ContactUsRequest contactUsRequest, SendEmailResult sendEmailResult) {
        awsClientFactory.getDynamoDB().getTable("ContactUsTable")
                        .putItem(new Item()
                                .withPrimaryKey("Id", sendEmailResult.getMessageId())
                                .withString("Subject", contactUsRequest.getSubject())
                                .withString("Username", contactUsRequest.getUsername())
                                .withString("Phone", contactUsRequest.getPhone())
                                .withString("Email", contactUsRequest.getEmail())
                                .withString("Question", contactUsRequest.getQuestion()));
    }

    private SendEmailResult sendEmail(LambdaLogger logger, ContactUsRequest contactUsRequest) {
        String emailTemplate = getEmailTemplate();
        String email = fillTemplate(emailTemplate, contactUsRequest);

        SendEmailRequest sendEmailRequest =
                new SendEmailRequest(
                        SENDER_EMAIL,
                        new Destination(Collections.singletonList(RECIPIENT_EMAIL)),
                        new Message()
                                .withSubject(
                                        new Content()
                                                .withCharset("UTF-8")
                                                .withData(contactUsRequest.getSubject()))
                                .withBody(new Body()
                                        .withHtml(new Content()
                                                .withCharset("UTF-8")
                                                .withData(email))));
        logger.log("Email template is ready");

        return awsClientFactory.getSesClient().sendEmail(sendEmailRequest);
    }

    private String fillTemplate(String emailTemplate, ContactUsRequest contactUsRequest) {
        return String.format(
                emailTemplate,
                contactUsRequest.getUsername(),
                contactUsRequest.getEmail(),
                contactUsRequest.getPhone(),
                contactUsRequest.getQuestion());
    }

    private String getEmailTemplate() {
        try {
            return IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("email_template.html")),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ContactUsRequest getContactUsRequest(AwsProxyRequest request) {
        try {
            return OBJECT_MAPPER.readValue(request.getBody(), ContactUsRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
