package babylon.search.lucene.index

import org.apache.lucene.index.FieldInvertState
import org.apache.lucene.search.similarities.ClassicSimilarity
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Nicolò Martini on 22/04/2017.
  */
class TFOnlySimilaritySpec extends WordSpec with Matchers {
    import TFOnlySimilaritySpec._

    "A TFOnlySimilarity" must {
        "return always 1 as Inverse Document Frequency" in {
            similarity.idf(1, 1) shouldBe 1f
            similarity.idf(12313, 23232) shouldBe 1f
        }
        "should decrease the norm if the length of the field increases" in {
            val state1 = new FieldInvertState("test")
            val state2 = new FieldInvertState("test")
            state1.setBoost(1)
            state1.setLength(1)
            state2.setBoost(1)
            state2.setLength(2)
            assert(similarity.lengthNorm(state1) > similarity.lengthNorm(state2))
        }
        "should have norms smaller than the classic similarity" in {
            val state1 = new FieldInvertState("test")
            val state2 = new FieldInvertState("test")
            state1.setBoost(1)
            state1.setLength(2)
            state2.setBoost(1)
            state2.setLength(3)
            assert(similarity.lengthNorm(state1) < classicSimilarity.lengthNorm(state1))
            assert(similarity.lengthNorm(state2) < classicSimilarity.lengthNorm(state2))
        }
    }
}

object TFOnlySimilaritySpec {
    val similarity = new TFOnlySimilarity
    val classicSimilarity = new ClassicSimilarity
}
