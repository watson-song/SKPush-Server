package handler

import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.concurrent.Eventually._
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import akka.actor.{ Props, ActorSystem }
import akka.testkit.{ TestActorRef, ImplicitSender, TestKit }
import com.github.mauricio.async.db.{ RowData, QueryResult }
import concurrent.Future
import org.mockito.Matchers._
import org.mockito.Matchers.{eq => equalTo}
import org.mockito.Mockito._
import org.mockito.{ ArgumentMatcher, Matchers }
import collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class DbHandlerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with MockitoSugar {

  def this() = this(ActorSystem("DbHandlerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "DbHandler.received" must {
    val testActorRef = TestActorRef[DbHandler](Props(new DbHandler(testActor)))
    val handler = testActorRef.underlyingActor
    val spyHandler = spy(handler)
    val mockInput = "mockInput"
    val mockQueryResultFuture = Future {
      mock[QueryResult]
    }

    doReturn(mockQueryResultFuture).when(spyHandler).execute(anyString(), anyString())
    doNothing().when(spyHandler).printAll()

    spyHandler.received(mockInput)
    "execute proper query with given parameter" in {
      class executeInputMatcher extends ArgumentMatcher[mutable.WrappedArray[Any]] {
        def matches(argument: Any) = {
          val array = argument.asInstanceOf[mutable.WrappedArray[Any]].array
          array.size == 1 && array(0).isInstanceOf[String] && array(0).asInstanceOf[String].startsWith(mockInput + "--")
        }
      }
      verify(spyHandler, times(1)).execute(equalTo("INSERT INTO demo VALUES (?)"), argThat(new executeInputMatcher))
    }
    "call printall in the end" in {
      verify(spyHandler, times(1)).printAll()
    }
  }

  "DbHandler.printAll" must {
    val testActorRef = TestActorRef[DbHandler](Props(new DbHandler(testActor)))
    val handler = testActorRef.underlyingActor

    val spyHandler = spy(handler)
    val mockRowData1 = mock[RowData]
    val mockRowData2 = mock[RowData]
    val mockResultSet = List(mockRowData1, mockRowData2)
    val mockData1 = "mockData1"
    val mockData2 = "mockData2"
    val mockResultSetResponse = Future {
      Option(mockResultSet)
    }

    doReturn(mockResultSetResponse).when(spyHandler).fetch(anyString(), any())
    doReturn(mockData1).when(spyHandler).getData(equalTo(mockRowData1))
    doReturn(mockData2).when(spyHandler).getData(equalTo(mockRowData2))

    spyHandler.printAll()
    "call respond on resultSet.flatMap" in {
      eventually {
        verify(spyHandler, atLeast(2)).respond(anyString())
        verify(spyHandler, times(1)).respond(equalTo(mockData1))
        verify(spyHandler, times(1)).respond(equalTo(mockData2))
      }
    }
    "parse each rowData" in {
      eventually {
        verify(spyHandler, times(2)).getData(any[RowData])
        verify(spyHandler, (times(1))).getData(equalTo(mockRowData1))
        verify(spyHandler, (times(1))).getData(equalTo(mockRowData2))
      }
    }
  }

  "DbHandler.getData" must {
    val testActorRef = TestActorRef[DbHandler](Props(new DbHandler(testActor)))
    val handler = testActorRef.underlyingActor

    val spyHandler = spy(handler)
    val mockRowData = mock[RowData]
    val mockData = "mockStringData"
    val mockDataNoCast = mockData.asInstanceOf[Any]

    doReturn(mockDataNoCast).when(mockRowData).apply(anyString())

    spyHandler.getData(mockRowData) must be(mockData)

    "get \"data\" field of row data" in {
      verify(mockRowData).apply(equalTo("data"))
    }
  }
}
