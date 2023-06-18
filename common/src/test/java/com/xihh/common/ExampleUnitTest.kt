package com.xihh.common

import org.junit.Test
import java.util.*
import kotlin.experimental.xor

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun jiami() {
        val s = "中国人不骗中国人".encodeToByteArray()
        s.forEachIndexed { index, c ->
            s[index] = c xor 1
        }

        println(s.decodeToString())
    }

    @Test
    fun jiemi() {
        val s = "幬䚼廻幌論幬䚼廻".encodeToByteArray()
        s.forEachIndexed { index, c ->
            s[index] = c xor 1
        }

        println(s.decodeToString())
    }

    @Test
    fun addition_isCorrect() {
        println(
            Solution().longestZigZag(
                TreeNode(
                    1,
                    null,
                    TreeNode(
                        1,
                        TreeNode(1),
                        TreeNode(
                            1,
                            TreeNode(
                                1,
                                null,
                                TreeNode(
                                    1,
                                    null,
                                    TreeNode(1)
                                )
                            ),
                            TreeNode(1)
                        )
                    )
                )
            )
        )
    }

    class Solution {
        fun lowestCommonAncestor(root: TreeNode?, p: TreeNode?, q: TreeNode?): TreeNode? {
            var 
        }
    }
}

data class ListNode(var `val`: Int, var next: ListNode? = null)

data class TreeNode @JvmOverloads constructor(
    @JvmField var `val`: Int,
    @JvmField var left: TreeNode? = null,
    @JvmField var right: TreeNode? = null,
)

val Char.isVowel
    get() = this.let {
        it == 'a' || it == 'e' || it == 'i' || it == 'o' || it == 'u' || it == 'A' || it == 'E' || it == 'I' || it == 'O' || it == 'U'
    }

class Codec() {
    // Encodes a URL to a shortened URL.
    fun serialize(root: TreeNode?): String {
        if (root == null) return "null"
        return root.`val`.toString() + "," + serialize(root.left) + "," + serialize(root.right)
    }

    // Decodes your encoded data to tree.
    fun deserialize(data: String): TreeNode? {
        return dfs(LinkedList(data.split(",")))
    }

    private fun dfs(queue: LinkedList<String>): TreeNode? {
        val node = queue.poll() ?: return null
        if (node == "null") return null
        val root = TreeNode(node.toInt())
        root.left = dfs(queue)
        root.right = dfs(queue)
        return root
    }
}

class QuickSort {
    fun sort(array: IntArray): IntArray {
        quick(array, 0, array.size - 1)
        return array
    }

    private fun quick(array: IntArray, left: Int, right: Int) {
        if (left >= right) {
            return
        }

        val pivot = partition(array, left, right)
        quick(array, left, pivot - 1)
        quick(array, pivot + 1, right)
    }

    private fun partition(array: IntArray, start: Int, end: Int): Int {
        var left = start
        var right = end
        val key = array[start]

        while (left < right) {
            while (left < right && array[right] >= key) {
                right--
            }
            while (left < right && array[left] <= key) {
                left++
            }
            swap(array, left, right)
        }

        swap(array, left, start)

        return left
    }

    private fun swap(array: IntArray, p1: Int, p2: Int) {
        val temp = array[p1]
        array[p1] = array[p2]
        array[p2] = temp
    }
}