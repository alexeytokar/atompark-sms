# atompark-sms
https://myatompark.com/ SMS client

Use as simple as:
````
AtomParkSmsSender sender = new AtomParkSmsSender(
                "your_public_key",
                "your_private_key"
);

//sender.getCampaignInfo( compain_id );
sender.sendMessage( "senderName", "some text", 380972223322 );
````
