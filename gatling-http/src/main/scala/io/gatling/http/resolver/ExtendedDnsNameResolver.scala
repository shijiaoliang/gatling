/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.http.resolver

import java.net.InetAddress
import java.util.{ List => JList }

import io.gatling.core.config.GatlingConfiguration

import com.typesafe.scalalogging.StrictLogging
import io.netty.channel.ChannelFactory
import io.netty.channel.EventLoop
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.codec.dns.DnsRecord
import io.netty.resolver.HostsFileEntriesResolver
import io.netty.resolver.dns._
import io.netty.util.concurrent.Promise

object ExtendedDnsNameResolver extends StrictLogging {

  val DebugEnabled = logger.underlying.isDebugEnabled

  val NioDatagramChannelFactory = new ChannelFactory[DatagramChannel] {
    override def newChannel(): DatagramChannel = new NioDatagramChannel
  }

  val DefaultDnsServerAddressStreamProvider = UnixResolverDnsServerAddressStreamProvider.parseSilently()
}

/**
 * DnsNameResolver whose sole purpose is to publicly expose the doResolve and executor methods that are protected
 * @param eventLoop the event loop
 * @param configuration the config
 */
class ExtendedDnsNameResolver(eventLoop: EventLoop, configuration: GatlingConfiguration)
    extends DnsNameResolver(
      eventLoop, // eventLoop
      ExtendedDnsNameResolver.NioDatagramChannelFactory, // channelFactory
      DnsServerAddresses.defaultAddresses(), // nameServerAddresses
      NoopDnsCache.INSTANCE, // resolveCache
      NoopDnsCache.INSTANCE, // authoritativeDnsServerCache
      configuration.http.dns.queryTimeout, // queryTimeoutMillis
      NettyDnsConstants.DefaultResolveAddressTypes, // resolvedAddressTypes
      true, // recursionDesired
      configuration.http.dns.maxQueriesPerResolve, // maxQueriesPerResolve
      ExtendedDnsNameResolver.DebugEnabled, // traceEnabled
      4096, // maxPayloadSize
      true, // optResourceEnabled
      HostsFileEntriesResolver.DEFAULT, // hostsFileEntriesResolver
      ExtendedDnsNameResolver.DefaultDnsServerAddressStreamProvider, // dnsServerAddressStreamProvider
      NettyDnsConstants.DefaultSearchDomain, // searchDomains
      1, // ndots
      true // decodeIdn
    ) {
  override def doResolve(inetHost: String, additionals: Array[DnsRecord], promise: Promise[InetAddress], resolveCache: DnsCache): Unit =
    super.doResolve(inetHost, additionals, promise, resolveCache)

  override def doResolveAll(inetHost: String, additionals: Array[DnsRecord], promise: Promise[JList[InetAddress]], resolveCache: DnsCache): Unit =
    super.doResolveAll(inetHost, additionals, promise, resolveCache)

  override def executor: EventLoop = super.executor()
}
