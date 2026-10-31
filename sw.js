// Trassihoiatus 1.20 – offline app shell and previously viewed map tiles.
const CORE='trassihoiatus-core-v120';
const TILES='trassihoiatus-maptiles-v1';
const ASSETS=[
 './','./index.html',
 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css',
 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js',
 'https://unpkg.com/proj4@2.12.1/dist/proj4.js',
 'https://unpkg.com/shpjs@6.1.0/dist/shp.js',
 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.min.js',
 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js'
];
async function cacheCore(){const c=await caches.open(CORE);const results=await Promise.allSettled(ASSETS.map(async url=>{const req=new Request(url,{mode:url.startsWith('http')?'cors':'same-origin'});const r=await fetch(req,{cache:'reload'});if(!r.ok)throw Error(url+': '+r.status);await c.put(req,r)}));if(results.some(x=>x.status==='rejected'))throw Error('Offline assets incomplete')}
self.addEventListener('install',event=>{event.waitUntil(cacheCore().then(()=>self.skipWaiting()))});
self.addEventListener('activate',event=>{event.waitUntil((async()=>{const names=await caches.keys();await Promise.all(names.filter(n=>n.startsWith('trassihoiatus-')&&n!==CORE&&n!==TILES).map(n=>caches.delete(n)));await self.clients.claim()})())});
self.addEventListener('message',event=>{if(event.data?.type==='CACHE_CORE')event.waitUntil(cacheCore())});
self.addEventListener('fetch',event=>{
 const req=event.request;if(req.method!=='GET')return;
 const url=new URL(req.url);if(!/^https?:$/.test(url.protocol))return;
 const isCore=ASSETS.some(asset=>new URL(asset,self.registration.scope).href===url.href);
 const isTile=/\/\d+\/\d+\/\d+\.(png|jpg|jpeg|webp)/i.test(url.pathname)||/\/wms\b/i.test(url.pathname)||url.hostname.includes('maaamet')||url.hostname.includes('openstreetmap');
 event.respondWith((async()=>{
  const cache=await caches.open(isCore?CORE:TILES);
  const cached=await cache.match(req);
  if(isCore&&cached)return cached;
  if(cached&&isTile)return cached;
  try{const response=await fetch(req);if(response.ok||response.type==='opaque'){
    if(isCore||isTile){try{await cache.put(req,response.clone())}catch(e){}}
  }return response}catch(e){return cached||Response.error()}
 })());
});
