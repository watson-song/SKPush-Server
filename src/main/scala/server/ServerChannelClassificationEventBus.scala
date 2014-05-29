package server

import akka.event.LookupClassification
import akka.event.ActorEventBus
import akka.actor.ActorRef
import akka.event.SubchannelClassification
import akka.util.Subclassification
import play.api.libs.json.JsValue
import akka.event.EventBus
import akka.util.ByteString
import util.Constants

/***
 * Message content for the push 
 */
case class Message(val id: String, val data: JsValue)

/***
 * Client will handle this event, for group and single messsage both
 */
case class MessageDispatchEvent(val channel: String, val message: Any)

/***
 * Provide channel event bus that we can seprate push message to different group of clients
 * */
object ServerChannelClassificationEventBus extends EventBus with SubchannelClassification {
  type Event = MessageDispatchEvent
  type Classifier = String
  type Subscriber = ActorRef
 
  protected def classify(event: Event): Classifier = event.channel
  
  protected def subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier) = x == y
    def isSubclass(x: Classifier, y: Classifier) = x.startsWith(y)
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event.message
  }
}