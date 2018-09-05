#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTextureSampler;
uniform sampler2D texture0;
varying vec2 vTextureCoord;
varying vec2 varyPostion;
//矩形融合区域
const vec2 leftBottom = vec2(-1.0, 0.40);
const vec2 rightTop = vec2(-0.40, 1.0);
void main()
{


   vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
   vec2 tex0 = vec2((varyPostion.x-leftBottom.x)/(rightTop.x-leftBottom.x),
                         1.0-(varyPostion.y-leftBottom.y)/(rightTop.y-leftBottom.y));
   vec4 color = texture2D(texture0, tex0);
//  gl_FragColor = color + texture2D(texture,v_TexCoordinate);
  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);
  gl_FragColor =color + vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);

}
