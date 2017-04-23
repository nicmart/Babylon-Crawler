package babylon.search.lucene.index

import org.apache.lucene.index.FieldInvertState
import org.apache.lucene.search.similarities.ClassicSimilarity

/**
  * A custom Lucene similarity based only on TF and that promotes
  * short fields
  */
class TFOnlySimilarity extends ClassicSimilarity {
    override def idf(docFreq: Long, docCount: Long): Float = 1f

    override def lengthNorm(state: FieldInvertState): Float = {
        var numTerms = 0
        if (discountOverlaps) numTerms = state.getLength - state.getNumOverlap
        else numTerms = state.getLength
        // Slighty prefer short fields (standard norm has exponent = 0.5)
        state.getBoost * (1.0 / Math.pow(numTerms, 0.55)).toFloat
    }
}
