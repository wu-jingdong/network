Android Network Protocol Stack
======================================================
Contains Udp Stack, Tcp Stack and Http Stack.

Udp Stack
---------------------------
	When send request, the protocol stack will add two bytes at the beginning of the business data.
	It means the length of the business data.
	The response data must begin with two bytes of length and business data as well. 
	The protocol stack will parse the length of the response data to validate the integrity.
	After verification, the protocol stack will set pure business data to the matched request.
	
	BusMessage must extends the BaseMessge and implements the abstract method in BaseMessage.
	The protocol stack will match the request when the response data reach.
	The BusMessage must implements the 'match' method to match request via the received business data.
	

Tcp Stack
---------------------------
	




Http Stack
---------------------------