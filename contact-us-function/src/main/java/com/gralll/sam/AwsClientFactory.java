/*
 * Copyright 2018: Thomson Reuters. All Rights Reserved. Proprietary and
 * Confidential information of Thomson Reuters. Disclosure, Use or Reproduction
 * without the written authorization of Thomson Reuters is prohibited.
 */
package com.gralll.sam;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;

public class AwsClientFactory {

    private static AmazonSimpleEmailService sesClient;
    private static AmazonDynamoDB dynamoDBClient;
    private static DynamoDB dynamoDB;

    public AwsClientFactory() {
        sesClient = AmazonSimpleEmailServiceClient.builder().withRegion(Regions.EU_WEST_1).build();
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();
        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    public DynamoDB getDynamoDB() {
        return dynamoDB;
    }

    public AmazonSimpleEmailService getSesClient() {
        return sesClient;
    }

    public AmazonDynamoDB getDynamoDBClient() {
        return dynamoDBClient;
    }
}