package ee.rmk.trassihoiatus

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

/** GeoJSON Polygon / MultiPolygon in WGS84 longitude-latitude order. Holes are respected. */
class Corridor private constructor(private val polygons: List<List<List<Pair<Double,Double>>>>) {
 companion object {
  fun parse(text: String): Corridor {
   val root = JSONObject(text)
   val polys = mutableListOf<List<List<Pair<Double,Double>>>>()
   fun ring(a: JSONArray): List<Pair<Double,Double>> = (0 until a.length()).map { i ->
    val p=a.getJSONArray(i); Pair(p.getDouble(0),p.getDouble(1))
   }
   fun polygon(a: JSONArray) = (0 until a.length()).map { ring(a.getJSONArray(it)) }
   fun geometry(g: JSONObject) { when(g.getString("type")) {
    "Polygon" -> polys.add(polygon(g.getJSONArray("coordinates")))
    "MultiPolygon" -> { val a=g.getJSONArray("coordinates"); for(i in 0 until a.length()) polys.add(polygon(a.getJSONArray(i))) }
   } }
   fun feature(f: JSONObject) { if(!f.isNull("geometry")) geometry(f.getJSONObject("geometry")) }
   when(root.getString("type")) {
    "FeatureCollection" -> { val a=root.getJSONArray("features"); for(i in 0 until a.length()) feature(a.getJSONObject(i)) }
    "Feature" -> feature(root)
    else -> geometry(root)
   }
   require(polys.isNotEmpty()) { "GeoJSON peab sisaldama Polygon või MultiPolygon geomeetriat" }
   return Corridor(polys)
  }
 }
 private fun insideRing(lon: Double, lat: Double, ring: List<Pair<Double,Double>>): Boolean {
  var inside=false
  for(i in ring.indices) {
   val a=ring[i]; val b=ring[(i+1)%ring.size]
   if ((a.second>lat)!=(b.second>lat) && lon < (b.first-a.first)*(lat-a.second)/(b.second-a.second)+a.first) inside=!inside
  }
  return inside
 }
 private fun inside(lon: Double,lat:Double):Boolean = polygons.any { p -> p.isNotEmpty() && insideRing(lon,lat,p[0]) && p.drop(1).none { insideRing(lon,lat,it) } }
 /** Signed nearest boundary distance, positive inside. Local equirectangular approximation. */
 fun signedDistanceMeters(lon:Double,lat:Double):Double {
  val kx=111320.0*cos(Math.toRadians(lat)); val ky=111132.0
  var best=Double.POSITIVE_INFINITY
  for(p in polygons) for(r in p) for(i in r.indices) {
   val a=r[i]; val b=r[(i+1)%r.size]
   val ax=(a.first-lon)*kx; val ay=(a.second-lat)*ky
   val bx=(b.first-lon)*kx; val by=(b.second-lat)*ky
   val dx=bx-ax; val dy=by-ay
   val t=if(dx*dx+dy*dy<1e-12) 0.0 else (-ax*dx-ay*dy)/(dx*dx+dy*dy)
   val u=t.coerceIn(0.0,1.0)
   best=min(best,hypot(ax+u*dx,ay+u*dy))
  }
  return if(inside(lon,lat)) best else -best
 }
}
