package com.billcoreatech.daycnt415.util

class Holidays : Comparable<Holidays> {
    // ArrayList의 type이 Comparable을 implements한 경우에만 sort 메소드의 정렬 기능을 사용할 수 있다
    lateinit var year // 연도
            : String
    lateinit var date // 월일
            : String
    lateinit var name // 휴일 명칭
            : String

    constructor() {}
    constructor(year: String, date: String, name: String) {
        this.year = year
        this.date = date
        this.name = name
    }

    override fun compareTo(o: Holidays): Int {
        return date!!.compareTo(o.date!!)
    }
}