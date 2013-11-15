Android Network Protocol Stack
======================================================
Contains Udp Stack, Tcp Stack and Http Stack.
A simple application framework.

Udp Stack
---------------------------
When send request, the protocol stack will add two bytes at the beginning of the business data.
It means the length of the business data.
The response data must begin with two bytes of length and business data as well. 
The protocol stack will parse the length of the response data to validate the integrity.
After verification, the protocol stack will set pure business data to the matched request.
	
BusiMessage must extends the BaseMessge and implements the abstract method in BaseMessage.
The protocol stack will match the request when the response data reach.
The BusiMessage must implements the 'match' method to match request via the received business data.

You can send data frequently(immediately, without delay) via the Udp stack, such as audio data or video data.

Tcp Stack
---------------------------
Have the same framework with the Udp Stack. But does not support frequent media-data.

Implements via nio. And have the combine-packet function.


Http Stack
---------------------------
There is a thread pool in the protocol stack(org.wjd.net.http.conn).
POOL_SIZE indicate the cached threads number that can be reused.
MAX_SIZE indicate the max request that can be sent at the same time.

The file upload-download function under org.wjd.net.http.file package.

Application framework
-------------------------------------------
![Framework Chart] (https://github.com/wu-jingdong/network/blob/master/framework.jpg)

Developed By
=============================================
wu-jingdong - donwujing@163.com