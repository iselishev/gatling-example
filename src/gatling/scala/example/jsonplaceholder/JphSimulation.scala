package example.jsonplaceholder

import java.io.{File, FileInputStream}
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import io.gatling.core.Predef._
import io.gatling.core.structure.{ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.language.postfixOps


class JphSimulation extends Simulation {

  val CONFIG_FOLDER = "./src/gatling/resources/jsonplaceholder/"
  val env = if (System.getProperty("env") != null) System.getProperty("env") else "prod"
  val configFileName = CONFIG_FOLDER + env + ".yml"
  val paramsFileName = CONFIG_FOLDER + "params_" + env + ".json"

  val configFile = new File(configFileName)
  if (!configFile.exists()) throw new IllegalArgumentException("Configuration file does not exist:" + configFileName)
  val paramsFile = new File(paramsFileName)
  if (!paramsFile.exists()) throw new IllegalArgumentException("JSON Feeder file does not exist:" + paramsFileName)

  val config = new Yaml(new Constructor(classOf[Config])).load(new FileInputStream(configFile)).asInstanceOf[Config]
  val jsonQueryFeeder = jsonFile(paramsFileName).circular

  val duration = config.getDuration minutes


  val httpProtocol: HttpProtocolBuilder = http.baseUrl(config.getEnvUrl)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  def testScenario: ScenarioBuilder = scenario("JsonPlaceholder API Checks")
    .during(duration) {
      feed(jsonQueryFeeder)
        .exec(scenario("Fetch all resources"))
        .exec(
          http("Get all records at /${resource}")
            .get("${resource}")
//            .check(regex("""id""").count.lte("${count}"))
        )
    }

  setUp(testScenario.inject(atOnceUsers(config.getUsers)))
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.gt(config.getGlobalThresholdResponsePassRate),
      global.responseTime.max.lt(config.getGatlingRequestMaxDurationTime))
    .protocols(httpProtocol)
    .maxDuration(duration + 60)
}
