package org.nkvoll.gpsd.client.commands

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class WatchSpec extends FunSpec with ShouldMatchers {
  describe("A Watch command") {
    it("serialize the json property if true") {
      val watch = Watch(enable=true, json=true)
      new String(watch.serialize()) should be ("""?WATCH={"enable":true,"json":true}""")
    }
    it("not serialize the json property if false") {
      val watch = Watch(enable=true, json=false)
      new String(watch.serialize()) should be ("""?WATCH={"enable":true}""")
    }
    it("serialize the enabled property even if false") {
      val watch = Watch(enable=false, json=false)
      new String(watch.serialize()) should be ("""?WATCH={"enable":false}""")
    }
  }
}
