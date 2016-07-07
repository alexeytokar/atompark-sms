package ua.net.tokar.atompark;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class AtomParkSmsSender {
    private static final String API_HOSTNAME = "https://api.myatompark.com/sms/";
    private static final String API_VERSION = "3.0";
    private final CloseableHttpClient httpclient;
    private final String publicKey;
    private final String privateKey;

    public AtomParkSmsSender( String publicKey, String privateKey ) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;

        this.httpclient = HttpClients.createDefault();
    }

    private void process( String action, List<NameValuePair> additionalParams ) {
        List<NameValuePair> params = new ArrayList<>();
        params.add( new BasicNameValuePair( "version", API_VERSION ) );
        params.add( new BasicNameValuePair( "action", action ) );

        params.addAll( additionalParams );

        params.add( new BasicNameValuePair( "key", publicKey ) );
        params.add( new BasicNameValuePair( "sum", calcSum( params ) ) );

        params.remove( 0 );
        params.remove( 0 );


        HttpPost httpPost = new HttpPost( String.format(
                "%s%s/%s",
                API_HOSTNAME,
                API_VERSION,
                action
        ) );
        httpPost.setEntity( new UrlEncodedFormEntity( params, Charset.forName( "UTF-8" ) ) );

        try ( CloseableHttpResponse response = httpclient.execute( httpPost ) ) {
            HttpEntity entity = response.getEntity();

            EntityUtils.consume( entity );

        } catch ( IOException e ) {
            // TODO log error
        }
    }

    private String calcSum( List<NameValuePair> params ) {
        Map<String, String> sortedTree = new TreeMap<>( new Comparator<String>() {
            @Override
            public int compare( String o1, String o2 ) {
                return o1.compareToIgnoreCase( o2 );
            }
        } );
        sortedTree.putAll( params.stream()
                                 .collect( Collectors.toMap(
                                         NameValuePair::getName,
                                         NameValuePair::getValue
                                 ) ) );
        StringBuilder result = new StringBuilder( "" );
        for ( Map.Entry<String, String> entry : sortedTree.entrySet() ) {
            result.append( entry.getValue() );
        }
        result.append( privateKey );

        return DigestUtils.md5Hex( result.toString() );
    }

    public void getCampaignInfo( int compaignId ) {
        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair( "id", String.valueOf( compaignId ) )
        );
        process( "getCampaignInfo", params );
    }

    public void sendMessage( String from, String text, long phone ) {
        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair( "sender", from ),
                new BasicNameValuePair( "text", text ),
                new BasicNameValuePair( "phone", String.valueOf( phone ) ),
                new BasicNameValuePair( "datetime", "" ),
                new BasicNameValuePair( "sms_lifetime", "6" )
        );

        process( "sendSMS", params );
    }

    public void getSenderStatus( int id ) {
        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair( "idName", String.valueOf( id ) )
        );

        process( "getSenderStatus", params );
    }
}