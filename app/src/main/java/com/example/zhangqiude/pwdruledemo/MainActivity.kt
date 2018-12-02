package com.example.zhangqiude.pwdruledemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                calculateSecurityScore(text.toString())
            }
        })

    }

    private fun calculateSecurityScore(text: String) {

        //密码强度规则：
        //
        //符合密码基本规则者为70分。
        //
        //    ＋(总字符数－8) × 4
        //    ＋(字母字符数－大写字母字符数) × (字母字符数 －小写字母字符数) × 2
        //    －(接连重复字符数) × 2
        //    －(接连数字字符数－3) × 1
        //    －(接连字母字符数－3) × 1
        //    －(3码以上的连续数字) × 3
        //    －(3码以上的连续字母) × 3
        //
        //规则：
        //    总分低於60者为弱(weak)，高于80者（含）为强(strong)，介於兩者之间为中等(medium)。
        //
        //举例：
        //1234567890a的最后分数
        //70+ （11-8）*4 +（1-0）*（1-1）*2 - （0*2） -（10-3）*1 - （0-3）*1 -（10*3）-（0*3）

        //字母字符数
        val letterCount = getLetterCount(text, 0)
        //小写字母字符数
        val lowerLetterCount = getLetterCount(text, 1)
        //大写字母字符数
        val upperLetterCount = getLetterCount(text, 2)
        //接连重复字符数
        val (repeatCount, repeatGroups) = getGroupCharCount(text, 0)
        //接连数字字符数
        val (continueDigitCount, continueDigitGroups) = getGroupCharCount(text, 1)
        //接连字母字符数
        val (continueLetterCount, continueLetterGroups) = getGroupCharCount(text, 2)
        //3码以上的连续数字数
        val (orderDigitCount, orderDigitGroups) = getOrderCharGroups(text, 0)
        //3码以上的连续字母数
        val (orderLetterCount, orderLetterGroups) = getOrderCharGroups(text, 1)

        var totalScore = 70

        //＋(总字符数－8) × 4
        val score1 = (text.length - 8) * 4
        totalScore += score1

        //＋(字母字符数－大写字母字符数) × (字母字符数 －小写字母字符数) × 2
        val score2 = (letterCount - upperLetterCount) * (letterCount - lowerLetterCount) * 2
        totalScore += score2

        //－(接连重复字符数) × 2
        val score3 = repeatCount * 2
        totalScore -= score3

        //－(接连数字字符数－3) × 1
        val score4 = (continueDigitCount - 3) * 1
        totalScore -= score4

        //－(接连字母字符数－3) × 1
        val score5 = (continueLetterCount - 3) * 1
        totalScore -= score5

        //－(3码以上的连续数字) × 3
        val score6 = orderDigitCount * 3
        totalScore -= score6

        //－(3码以上的连续字母) × 3
        val score7 = orderLetterCount * 3
        totalScore -= score7

        val textShow = "总字符数 = ${text.length}\n" +
                "字母字符数 = $letterCount\n" +
                "小写字母字符数 = $lowerLetterCount\n" +
                "大写字母字符数 = $upperLetterCount\n" +
                "接连重复字符数 = $repeatCount\n$repeatGroups\n" +
                "接连数字字符数 = $continueDigitCount\n$continueDigitGroups\n" +
                "接连字母字符数 = $continueLetterCount\n$continueLetterGroups\n" +
                "3码以上的连续数字数 = $orderDigitCount\n$orderDigitGroups\n" +
                "3码以上的连续字母数 = $orderLetterCount\n$orderLetterGroups\n" +
                "总分：$totalScore = 70 + ($score1) + ($score2) - ($score3) - ($score4) - ($score5) - ($score6) - ($score7)"
        Log.v(
            TAG, textShow
        )

        securityScore.text = textShow

    }

    private fun getOrderCharGroups(text: String, type: Int): Pair<Int, MutableList<String>> {

        val indices = text.indices

        val groups = mutableListOf<String>()

        var hasNext: Boolean
        var startIndex = 0 //每组连续字符的开始位置索引
        var hitCount = 0 //每组连续字符的个数
        var totalCount = 0 //所有连续字符的总个数
        for (index in indices) {
            hitCount += 1
            val curChar = text[index]
            //是否有连续的下一项
            hasNext = if (index + 1 in indices) {
                val nextChar = text[index + 1]
                curChar + 1 == nextChar
            } else {
                false
            }

            //连续字符结束，记录起来
            if (!hasNext) {
                if (hitCount >= 3) {
                    if (type == 0 && curChar.isDigit()) { //数字连续字符
                        totalCount += hitCount
                        groups.add(text.substring(startIndex, startIndex + hitCount))
                    } else if (type == 1 && curChar.isLetter()) { //字母连续字符
                        totalCount += hitCount
                        groups.add(text.substring(startIndex, startIndex + hitCount))
                    }

                }

                hitCount = 0
                startIndex = index + 1
            }
        }

        return totalCount to groups

    }

    private fun getGroupCharCount(text: String, type: Int): Pair<Int, MutableList<String>> {
        val matcher = when (type) {
            0 -> repeatGroupPattern.matcher(text)
            1 -> digitGroupPattern.matcher(text)
            else -> letterGroupPattern.matcher(text)
        }
        var count = 0
        val groups = mutableListOf<String>()
        while (matcher.find()) {
            val group = matcher.group()
            count += group.length
            groups.add(group)
        }
        return count to groups
    }

    private fun getLetterCount(text: String, type: Int): Int {
        val matcher = when (type) {
            1 -> lowerLetterPattern.matcher(text)
            2 -> upperLetterPattern.matcher(text)
            else -> letterPattern.matcher(text)
        }

        var count = 0
        while (matcher.find()) {
            count += matcher.group().length
        }
        return count
    }

    companion object {
        const val TAG = "PWD"
        val letterPattern = Pattern.compile("[a-zA-Z]+")!!
        val lowerLetterPattern = Pattern.compile("[a-z]+")!!
        val upperLetterPattern = Pattern.compile("[A-Z]+")!!
        val repeatGroupPattern = Pattern.compile("([0-9a-zA-Z])\\1+")!!
        val digitGroupPattern = Pattern.compile("[0-9]{2,}")!!
        val letterGroupPattern = Pattern.compile("[a-zA-Z]{2,}")!!
    }

}
