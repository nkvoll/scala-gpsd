package org.nkvoll.gpsd.client.akka

import util.Random

class RetryTimer(maxDelay: Double = 3600000, initialDelay: Double = 1000, factor: Double = Math.E, jitter: Double = 0.11962656472) {
  private var retries = 0
  private var currentDelay = initialDelay

  def resetDelay() {
    currentDelay = initialDelay
    retries = 0
  }

  def getDelay = {
    currentDelay = Math.min(currentDelay * factor, maxDelay)
    if (jitter != 0) {
      currentDelay = normalVariate(currentDelay, currentDelay * jitter)
    }

    currentDelay.asInstanceOf[Long]
  }

  def getDelayAndIncrementRetries = {
    currentDelay = Math.min(currentDelay * factor, maxDelay)
    if (jitter != 0) {
      currentDelay = normalVariate(currentDelay, currentDelay * jitter)
    }

    retries += 1

    currentDelay.asInstanceOf[Long]
  }

  private def normalVariate(mu: Double, sigma: Double) = Random.nextGaussian()*sigma + mu
}

object RetryTimer {
  def getDefaultRetryTimer = new RetryTimer(maxDelay=30000, initialDelay = 500, factor = Math.PI/2)
}
