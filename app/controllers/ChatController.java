package controllers;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.JsonNode;
import org.reactivestreams.Subscriber;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by jabelardo on 12/8/16.
 */
@Singleton
public class ChatController extends Controller {

//    private Map<String, List<LegacyEventSource>> legacySocketsPerRoom = new HashMap<>();

    private final Map<String, Map<String, Subscriber<? super JsonNode>>> subscribersPerRoom = new HashMap<>();

    private final Materializer materializer;

    @Inject
    public ChatController(Materializer materializer) {
        this.materializer = materializer;
    }

    public Result indexRiot() {
        return ok(views.html.riot.render("Chat using Server Sent Events and Riot"));
    }

    /**
     * Controller action for POSTing chat messages
     */
    public Result postMessage() {
        JsonNode body = request().body().asJson();
        pushChatMsg(body);
//        System.out.println(request().remoteAddress() + " - SSE posted message " + body);
        return ok();
    }

    public void pushChatMsg(JsonNode msg) {
        String room = msg.findPath("room").textValue();

        System.out.println("SSE posted message " + msg);

//        if (legacySocketsPerRoom.containsKey(room)) {
//            legacySocketsPerRoom.get(room).forEach(es -> es.send(LegacyEventSource.Event.event(msg)));
//        }

        if (subscribersPerRoom.containsKey(room)) {
            subscribersPerRoom.get(room).forEach((ip, subs) -> {
                subs.onNext(msg);
//                subs.onComplete();
            });
        }
    }

    /**
     * Establish the SSE HTTP 1.1 connection.
     * The new EventSource socket is stored in the socketsPerRoom Map
     * to keep track of which browser is in which room.
     * <p>
     * onDisconnected removes the browser from the socketsPerRoom Map if the
     * browser window has been exited.
     *
     * @return the feed
     */
    public Result chatFeed(String room) {
        return publisherChatFeed(room);
    }

//    private Result LegacyChatFeed(String room) {
//        String remoteAddress = request().remoteAddress();
//
//        return ok(new LegacyEventSource() {
//            @Override
//            public void onConnected() {
//                LegacyEventSource currentSocket = this;
//
//                this.onDisconnected(() -> {
//                    System.out.println(remoteAddress + " - SSE disconntected room " + room);
//                    legacySocketsPerRoom.compute(room, (key, value) -> {
//                        if (value.contains(currentSocket))
//                            value.remove(currentSocket);
//                        return value;
//                    });
//                });
//
//                // Add socket to room
//                legacySocketsPerRoom.compute(room, (key, value) -> {
//                    if (value == null)
//                        return new ArrayList<LegacyEventSource>() {{
//                            add(currentSocket);
//                        }};
//                    else
//                        value.add(currentSocket);
//                    return value;
//                });
//
//                System.out.println(remoteAddress + " - SSE conntected room " + room);
//            }
//        });
//    }

    private Result publisherChatFeed(String room) {
        String remoteAddress = request().remoteAddress();

        Source<JsonNode, NotUsed> source = Source.fromPublisher(subscriber ->
                subscribersPerRoom.compute(room, (key, subscribers) -> {
                    if (subscribers == null) {
                        return new HashMap<String, Subscriber<? super JsonNode>>() {{
                            System.out.println(remoteAddress + " - SSE connected (1) to room " + room);
                            put(remoteAddress, subscriber);
                        }};
                    } else {
                        if (subscribers.put(remoteAddress, subscriber) == null) {
                            System.out.println(remoteAddress + " - SSE connected (" + subscribers.size() + ") to room " + room);
                        } else {
                            System.out.println(remoteAddress + " - SSE reconnected (" + subscribers.size() + ") to room " + room);
                        }
                    }
                    return subscribers;
                }));

        return ok().chunked(source
                .watchTermination((o, p) -> {
                    subscribersPerRoom.forEach((aRoom, subscribers) -> {
                        if (!aRoom.equals(room)) {
                            Subscriber<? super JsonNode> subscriber = subscribers.remove(remoteAddress);
                            // subscriber.onComplete();
                            System.out.println(remoteAddress + " - SSE disconnected from room " + aRoom);
                        }
                    });
                    return p;
                })
                .filter(json -> json.path("room").asText().equals(room))
                .keepAlive(FiniteDuration.create(30, SECONDS), () -> {
                    System.out.println(remoteAddress + " - SSE ping to room " + room);
                    return Json.newObject();
                })
                .map(EventSource.Event::event)
                .via(EventSource.flow()))
                .as(Http.MimeTypes.EVENT_STREAM);
    }
}
