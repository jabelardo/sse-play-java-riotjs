package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.ChatController;
import org.joda.time.DateTime;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by jabelardo on 12/8/16.
 */
@Singleton
public class ChatActors {

    /**
     * SSE-Chat actor system
     */
    private final ActorSystem system = ActorSystem.create("sse-chat-java-riot");

    @Inject
    public ChatActors(ApplicationLifecycle lifecycle, ChatController chatController) {
        system.actorOf(Props.create(Supervisor.class, chatController), "ChatterSupervisor");

        lifecycle.addStopHook(() -> CompletableFuture.completedFuture(system.terminate()));
    }
}

class Talk {
    final String room;

    Talk(String room) {
        this.room = room;
    }
}

/**
 * Supervisor initiating Romeo and Juliet actors and scheduling their talking
 */
class Supervisor extends UntypedActor {

    Supervisor(ChatController chatController) {
        ActorRef juliet = getContext().actorOf(Props.create(Chatter.class, "Juliet", Quotes.juliet, chatController));

        getContext().system().scheduler().schedule(FiniteDuration.create(1, TimeUnit.SECONDS),
                FiniteDuration.create(8, TimeUnit.SECONDS), juliet, new Talk("room1"),
                getContext().dispatcher(), ActorRef.noSender());


        ActorRef romeo = getContext().actorOf(Props.create(Chatter.class, "Romeo", Quotes.romeo, chatController));

        getContext().system().scheduler().schedule(FiniteDuration.create(1, TimeUnit.SECONDS),
                FiniteDuration.create(8, TimeUnit.SECONDS), romeo, new Talk("room1"),
                getContext().dispatcher(), ActorRef.noSender());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
    }
}

/**
 * Chat participant actors picking quotes at random when told to talk
 */
class Chatter extends UntypedActor {

    private final String name;
    private final String[] quotes;
    private final ChatController chatController;

    Chatter(String name, String[] quotes, ChatController chatController) {
        this.name = name;
        this.quotes = quotes;
        this.chatController = chatController;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Talk) {
            String now = DateTime.now().toString();
            String quote = quotes[SecureRandom.getInstanceStrong().nextInt(quotes.length)];
            JsonNode msg = Json.newObject().put("room", ((Talk) message).room).put("text", quote).put("user", name).put("time", now);
            chatController.pushChatMsg(msg);
        }
    }
}

class Quotes {
    static final String[] juliet = {
            "O Romeo, Romeo! wherefore art thou Romeo? \nDeny thy father and refuse thy name; \nOr, if thou wilt not, be but sworn my love, \nAnd I'll no longer be a Capulet. ",
            "By whose direction found'st thou out this place?",
            "I would not for the world they saw thee here.",
            "What man art thou that, thus bescreened in night, \nSo stumblest on my counsel?",
            "If they do see thee, they will murder thee.",
            "My ears have yet not drunk a hundred words \n Of thy tongue's uttering, yet I know the sound.\nArt thou not Romeo, and a Montague?",
            "How cam'st thou hither, tell me, and wherefore?\nThe orchard walls are high and hard to climb,\nAnd the place death, considering who thou art,\nIf any of my kinsmen find thee here."
    };

    static final String[] romeo = {
            "Neither, fair saint, if either thee dislike.",
            "With love's light wings did I o'erperch these walls,\nFor stony limits cannot hold love out,\nAnd what love can do, that dares love attempt:\n Therefore thy kinsmen are no stop to me.",
            "Alack, there lies more peril in thine eye \nThan twenty of their swords. Look thou but sweet\nAnd I am proof against their enmity.",
            "By a name\nI know not how to tell thee who I am:\nMy name, dear saint, is hateful to myself,\nBecause it is an enemy to thee.\nHad I it written, I would tear the word.",
            "I have night's cloak to hide me from their eyes, \nAnd, but thou love me, let them find me here;\nMy life were better ended by their hate\nThan death prorogued, wanting of thy love.",
            "I take thee at thy word.\n Call me but love, and I'll be new baptis'd;\nHenceforth I never will be Romeo.",
            "By love, that first did prompt me to enquire.\n He lent me counsel, and I lent him eyes.\n I am no pilot, yet, wert thou as far\nAs that vast shore wash'd with the furthest sea, \nI should adventure for such merchandise.",
            "[Aside.] Shall I hear more, or shall I speak at this?"
    };
}



