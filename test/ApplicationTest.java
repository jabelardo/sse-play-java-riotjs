import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void send404OnABadRequest() {
        running(fakeApplication(), () -> {
            assertThat(route(fakeRequest(GET, "/boum")).status(), is(NOT_FOUND));
        });
    }

    @Test
    public void renderTheIndexPage() {
        running(fakeApplication(), () -> {
            Result home = route(fakeRequest(GET, "/"));
            assertThat(home.status(), is(OK));
            assertThat(home.contentType().get(), is(Http.MimeTypes.HTML));
            assertThat(contentAsString(home), containsString("Chat using Server Sent Events and Riot"));
            // assertThat(contentAsString(home), containsString("Your Name:"));
        });
    }
}
