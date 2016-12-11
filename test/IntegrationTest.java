import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void workFromWithinABrowser() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertThat(browser.pageSource(), containsString("Chat using Server Sent Events and Riot"));
        });
    }

    @Test
    public void deliverJsonOverSseFilteredByChatRoom() {
//        running(testServer(3333)), () -> {
//
//            /** Folding Iteratee, parses Array[Byte] chunks and accumulates them into a Seq[JsValue] */
//            def concatChunks = Iteratee.fold[Array[Byte], Seq[ JsValue]](Seq[JsValue] ()){
//                (acc, chunk) =>acc:
//                +Json.parse((new String(chunk, "UTF-8")).replace("data: ", ""))
//            }
//
//            /** Iteratee above, chained with Enumeratee to reach Done state after n steps*/
//            def take (n:Int) =Enumeratee.take(n) &>>concatChunks
//            val n = 3 // number of elements for Iteratee to consume
//            val chatRoom = "room1"
//
//            /** Test data:  */
//            val msgs = Seq(
//                    Json.toJson(Msg("room1", "message1", "user", "date")),
//                    Json.toJson(Msg("room2", "message2", "user", "date")),
//                    Json.toJson(Msg("room1", "message3", "user", "date")),
//                    Json.toJson(Msg("room3", "message4", "user", "date")),
//                    Json.toJson(Msg("room1", "message5", "user", "date")),
//                    Json.toJson(Msg("room1", "message6", "user", "date"))
//            )
//
//            /** workaround for problems matching within await{}.map{} */
//            var resultSeq = Seq[JsValue] ()
//
//            /** start stream consuming connection for chat room feed */
//            val client = WS.url("http://localhost:3333/chatFeed/" + chatRoom).get(_ = > take(n))
//
//            /** post six messages from Seq above in chat endpoint */
//            msgs.foreach(msg = > {Thread.sleep(250); WS.url("http://localhost:3333/chat").post(msg) } )
//
//            /** await result of take(n) Iteratee, map result to resultSeq for comparison when done */
//            Await.result(client, Duration(5, "seconds")).map {
//                seq =>resultSeq = seq
//            }
//
//            /** result must be equal to n elements taken from msgs, filtered by chatRoom */
//            resultSeq.length must beEqualTo(n)
//            resultSeq must beEqualTo(msgs.filter(json = > (json \ "room").as[String] == chatRoom).take(n))
//        }
    }
}