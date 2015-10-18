# File Fetcher



<div class="jd-descr">

<h2 style="margin-bottom: 0px;">URL Reference</h2><hr>

<p><a href="http://developer.android.com/reference/java/net/URL.html">http://developer.android.com/reference/java/net/URL.html</a></p>

<h3 style="margin-bottom: 0px;">Class Overview</h3><hr>
<p itemprop="articleBody">A Uniform Resource Locator that identifies the location of an Internet
 resource as specified by <a href="http://www.ietf.org/rfc/rfc1738.txt">RFC
 1738</a>.

 </p><h3>Parts of a URL</h3>
 A URL is composed of many parts. This class can both parse URL strings into
 parts and compose URL strings from parts. For example, consider the parts of
 this URL:
 <code>http://username:password@host:8080/directory/file?query#ref</code>:
 <table>
 <tbody><tr><th>Component</th><th>Example value</th><th>Also known as</th></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getProtocol()">Protocol</a></code></td><td><code>http</code></td><td>scheme</td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getAuthority()">Authority</a></code></td><td><code>username:password@host:8080</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getUserInfo()">User Info</a></code></td><td><code>username:password</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getHost()">Host</a></code></td><td><code>host</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getPort()">Port</a></code></td><td><code>8080</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getFile()">File</a></code></td><td><code>/directory/file?query</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getPath()">Path</a></code></td><td><code>/directory/file</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getQuery()">Query</a></code></td><td><code>query</code></td><td></td></tr>
 <tr><td><code><a href="http://developer.android.com/reference/java/net/URL.html#getRef()">Ref</a></code></td><td><code>ref</code></td><td>fragment</td></tr>
 </tbody></table>

 <h3>Supported Protocols</h3>
 This class may be used to construct URLs with the following protocols:
 <ul>
 <li><strong>file</strong>: read files from the local filesystem.
 </li><li><strong>ftp</strong>: <a href="http://www.ietf.org/rfc/rfc959.txt">File
     Transfer Protocol</a>
 </li><li><strong>http</strong>: <a href="http://www.ietf.org/rfc/rfc2616.txt">Hypertext
     Transfer Protocol</a>
 </li><li><strong>https</strong>: <a href="http://www.ietf.org/rfc/rfc2818.txt">HTTP
     over TLS</a>
 </li><li><strong>jar</strong>: read <code><a href="http://developer.android.com/reference/java/util/jar/JarFile.html">Jar files</a></code> from the
     filesystem</li>
 </ul>
 In general, attempts to create URLs with any other protocol will fail with a
 <code><a href="http://developer.android.com/reference/java/net/MalformedURLException.html">MalformedURLException</a></code>. Applications may install handlers for other
 schemes using <code><a href="http://developer.android.com/reference/java/net/URL.html#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)">setURLStreamHandlerFactory(URLStreamHandlerFactory)</a></code> or with the <code>java.protocol.handler.pkgs</code> system property.

 <p>The <code><a href="http://developer.android.com/reference/java/net/URI.html">URI</a></code> class can be used to manipulate URLs of any protocol.
</p>

</div>