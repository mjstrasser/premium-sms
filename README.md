# Premium SMS

Experimenting with [Ktor](https://ktor.io) services by implementing a rudimentary premium SMS
solution.

## Background

Premium SMS is the name for services that are provided when a message is sent to a certain
number, for a specific charge.
For example, premium SMS is one of the mechanisms for registering votes in some
public competitions, e.g. Eurovision Song Contest. 

### Short Message Service (SMS)

Parts of the SMS ecosystem are:

* [MSISDN](https://en.wikipedia.org/wiki/MSISDN): internationally-unique number that identifies
  a mobile service, e.g. 61412345678.
* Mobile Originated (MO) messages, sent from a mobile device.
* Mobile Terminated (MT) messages, sent to a mobile device.
* [Short message service centre (SMSC)](https://en.wikipedia.org/wiki/Short_Message_service_center):
  A system used by mobile telcos to route and manage SMS messages.
* [External short message entity (ESME)](https://en.wikipedia.org/wiki/External_Short_Messaging_Entity):
  A system external to mobile-to-mobile messaging. A Premium SMS system is an example of
  an ESME.
* [Short message peer-to-peer (SMPP)](https://en.wikipedia.org/wiki/Short_Message_Peer-to-Peer): a
  binary protocol for exchanging messages between components, e.g. SMSC and ESME.

## Sequence

Someone wants to pay for a service using their mobile phone account. They send a message
to a Premium SMS service number and a fee is debited from their account. Each service has
a charge associated with it (e.g. 55 cents).

1. The SMSC routes the MO message to the Premium SMS system using SMPP or HTTP.

2. The Premium SMS system needs to determine if it can debit the charge to the account.
   Considerations are:
   
   * Does this account permit Premium SMS?
   * Is the service flagged as adult content? Does this account permit adult content (e.g.
     is the user under age)?
   * Is the account pre-paid or post-paid? Post-paid accounts are considered to have
     sufficient funds in all cases; pre-paid accounts must have their balance checked in
     real time.

3. After applying the charge, the message is sent to the external service provider by calling
   its API.

4. The service provider acknowledges successful receipt of the MO message with an MT message.
   If an error occurs, the Premium SMS system can reverse the charge on the mobile account and
   send an appropriate MT message.

These steps should be asynchronous.

### Notes

* Message contents are passed through unchanged. Premium SMS systems can charge or route
  messages differently according to message contents.

## Components of this Premium SMS system

### Main service

* Accepts MO message from SMSC.
* Maintains the database of Premium SMS services.
* Calls **Mobile service service** to check if Premium SMS is enabled and if age restrictions apply.
* Calls **Charging service** to apply the charge.
* Calls the provider API with the message.
* *Future*: Sends MT messages to the SMSC.
* *Future*: Accepts callbacks from the service provider.

### Mobile service service

* Accepts account requests from the **Main service**
* Determines if the mobile service has Premium SMS enabled and if age restrictions apply.

### Charging service

* Accepts charging requests from the **Main service**
* Determines if the account is pre-paid or post-paid.
* Determines if a pre-paid account has sufficient funds.
