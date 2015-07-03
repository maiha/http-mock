package sc.ala.http.mock

import java.lang.AssertionError

class ExpectCountSpec extends TestHelper {
  val url : String = s"http://127.0.0.1:$testPort"

  describe("count") {
    it("passes an assertion about counting accesslogs") {
      HttpMock.run(testPort) { server =>
        get(url)
        server.logs.expect(GET, count = 1)  (timeout)
      }
    }

    it("throws AssertionError when not fully accessed") {
      HttpMock.run(testPort) { server =>
        get(url)
        intercept[AssertionError] {
          server.logs.expect(GET, count = 2)  (timeout)
        }
      }
    }

    it("throws AssertionError when method is different") {
      HttpMock.run(testPort) { server =>
        post(url, "body")
        intercept[AssertionError] {
          server.logs.expect(GET, count = 1)  (timeout)
        }
      }
    }
  }
}
