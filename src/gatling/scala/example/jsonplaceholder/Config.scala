package example.jsonplaceholder

import scala.beans.BeanProperty

class Config {
  @BeanProperty var envUrl = ""
  @BeanProperty var duration:Integer = 1
  @BeanProperty var users = 2
  @BeanProperty var getGatlingRequestMaxDurationTime = 10000
  @BeanProperty var globalThresholdResponsePassRate = 95
  @BeanProperty var rampUp = 4

}