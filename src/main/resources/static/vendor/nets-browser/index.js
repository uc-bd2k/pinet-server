var req = require("xhr");

module.exports = Nets;

function Nets(opts, cb) {
  if (typeof opts === "string") opts = { uri: opts };

  if (!opts.hasOwnProperty("encoding")) opts.encoding = null;

  if (!opts.hasOwnProperty("json") && opts.encoding === null) {
    opts.responseType = "arraybuffer";
    var originalCb = cb;
    cb = bufferify;
  }

  function bufferify(err, resp, body) {
    if (body) body = Buffer.from(new Uint8Array(body));
    originalCb(err, resp, body);
  }

  return req(opts, cb);
}
