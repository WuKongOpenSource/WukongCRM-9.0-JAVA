/**
 * Created by jiachenpan on 16/11/18.
 */

export function parseTime(time, cFormat) {
  if (arguments.length === 0) {
    return null
  }
  const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (('' + time).length === 10) time = parseInt(time) * 1000
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

export function formatTime(time, option) {
  time = +time * 1000
  const d = new Date(time)
  const now = Date.now()

  const diff = (now - d) / 1000

  if (diff < 30) {
    return '刚刚'
  } else if (diff < 3600) {
    // less 1 hour
    return Math.ceil(diff / 60) + '分钟前'
  } else if (diff < 3600 * 24) {
    return Math.ceil(diff / 3600) + '小时前'
  } else if (diff < 3600 * 24 * 2) {
    return '1天前'
  }
  if (option) {
    return parseTime(time, option)
  } else {
    return (
      d.getMonth() +
      1 +
      '月' +
      d.getDate() +
      '日' +
      d.getHours() +
      '时' +
      d.getMinutes() +
      '分'
    )
  }
}

export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/** 压缩文件
 * quality压缩百分比 0.3
 */
export function compressImage(file, quality, callback) {
  // quality 设置为0.3
  quality = quality || 0.3
  const reader = new FileReader()
  reader.onload = function (event) {
    var result = event.target.result
    if (file.size > 204800 && file.type !== 'image/gif' && quality < 1) { // 大于200Kb
      const img = new Image()
      img.src = result
      img.onload = function () {
        // 如果图片大于四百万像素，计算压缩比并将大小压至400万以下
        var initSize = img.src.length
        var width = img.width
        var height = img.height

        var ratio
        if ((ratio = width * height / 4000000) > 1) {
          ratio = Math.sqrt(ratio)
          width /= ratio
          height /= ratio
        } else {
          ratio = 1
        }
        var canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        // 铺底色
        var ctx = canvas.getContext('2d')
        ctx.fillStyle = '#fff'
        ctx.fillRect(0, 0, canvas.width, canvas.height)
        // 如果图片像素大于100万则使用瓦片绘制
        var count
        if ((count = width * height / 1000000) > 1) {
          count = ~~(Math.sqrt(count) + 1)
          // 计算要分成多少块瓦片
          // 计算每块瓦片的宽和高
          var nw = ~~(width / count)
          var nh = ~~(height / count)
          var tCanvas = document.createElement('canvas')
          tCanvas.width = nw
          tCanvas.height = nh
          for (var i = 0; i < count; i++) {
            for (var j = 0; j < count; j++) {
              var tctx = tCanvas.getContext('2d')
              tctx.drawImage(img, i * nw * ratio, j * nh * ratio, nw * ratio, nh * ratio, 0, 0, nw, nh)

              ctx.drawImage(tCanvas, i * nw, j * nh, nw, nh)
            }
          }
          tCanvas.width = tCanvas.height = 0
        } else {
          ctx.drawImage(img, 0, 0, width, height)
        }
        // 进行最小压缩
        var ndata = canvas.toDataURL('image/jpeg', quality)
        canvas.width = canvas.height = 0
        callback(ndata)
      }
    } else { // 小于200K不需要压缩 直接返回
      callback(result)
    }
  }
  reader.readAsDataURL(file)
}

/** 根据date URL 创建blob 用于上传 */
export function createBlob(result) {
  var arr = result.split(',')
  var mime = arr[0].match(/:(.*?)/)[1]
  var bstr = atob(arr[1])
  var n = bstr.length
  var u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], {
    type: mime
  })
}

/** 获取file大小的名称 */
export function fileSize(size) {
  var size_int = size
  if (typeof size === 'string' && size.constructor == String) {
    size_int = parseInt(size)
  }
  var formatSize
  if (parseInt(size_int / 1024 / 1024) > 0) {
    formatSize = (size_int / 1024 / 1024).toFixed(2) + 'MB'
  } else if (parseInt(size_int / 1024) > 0) {
    formatSize = (size_int / 1024).toFixed(2) + 'kB'
  } else {
    formatSize = size_int + 'Byte'
  }
  return formatSize
}

/** 获取最大 z-index 的值 */
import {
  PopupManager
} from 'element-ui/lib/utils/popup'
export function getMaxIndex() {
  return PopupManager.nextZIndex()
}

/** 深拷贝 */
export function objDeepCopy(source) {
  var sourceCopy = source instanceof Array ? [] : {}
  for (var item in source) {
    if (!source[item]) {
      sourceCopy[item] = source[item]
    } else {
      sourceCopy[item] = typeof source[item] === 'object' ? objDeepCopy(source[item]) : source[item]
    }
  }
  return sourceCopy
}

/** 获取文件类型图标 */
export function getFileTypeIcon(file) {
  if (file.type.indexOf('image') !== -1) {
    return require('@/assets/img/file_img.png')
  } else if (file.type.indexOf('audio') !== -1 || file.type.indexOf('video') !== -1) {
    return require('@/assets/img/file_video.png')
  } else {
    var index = file.name.lastIndexOf('.')
    var ext = file.name.substr(index + 1)
    if (arrayContain(['xlsx', 'xls', 'XLSX', 'XLS'], ext)) {
      return require('@/assets/img/file_excle.png')
    } else if (arrayContain(['doc', 'docx', 'DOC', 'DOCX'], ext)) {
      return require('@/assets/img/file_word.png')
    } else if (arrayContain(['rar', 'zip'], ext)) {
      return require('@/assets/img/file_zip.png')
    } else if (ext === 'pdf') {
      return require('@/assets/img/file_pdf.png')
    } else if (ext === 'ppt' || ext === 'pptx') {
      return require('@/assets/img/file_ppt.png')
    } else if (arrayContain(['txt', 'text'], ext)) {
      return require('@/assets/img/file_txt.png')
    }
  }
  return require('@/assets/img/file_unknown.png')
}

export function getFileTypeIconWithSuffix(ext) {
  if (arrayContain(['jpg', 'png', 'gif'], ext)) {
    return require('@/assets/img/file_img.png')
  } else if (arrayContain(['mp4', 'mp3', 'avi'], ext)) {
    return require('@/assets/img/file_excle.png')
  } else if (arrayContain(['xlsx', 'xls', 'XLSX', 'XLS'], ext)) {
    return require('@/assets/img/file_excle.png')
  } else if (arrayContain(['doc', 'docx', 'DOC', 'DOCX'], ext)) {
    return require('@/assets/img/file_word.png')
  } else if (arrayContain(['rar', 'zip'], ext)) {
    return require('@/assets/img/file_zip.png')
  } else if (ext === 'pdf') {
    return require('@/assets/img/file_pdf.png')
  } else if (ext === 'ppt' || ext === 'pptx') {
    return require('@/assets/img/file_ppt.png')
  } else if (arrayContain(['txt', 'text'], ext)) {
    return require('@/assets/img/file_txt.png')
  }
  return require('@/assets/img/file_unknown.png')
}

function arrayContain(array, string) {
  return array.some((item) => {
    return item === string
  })
}

/** 判断输入的是number */
export function regexIsNumber(nubmer) {
  var regex = /^[0-9]+.?[0-9]*/
  if (!regex.test(nubmer)) {
    return false
  }
  return true
}

/** 判断输入的是crm数字 数字的整数部分须少于12位，小数部分须少于4位*/
export function regexIsCRMNumber(nubmer) {
  var regex = /^([-+]?\d{1,12})(\.\d{0,4})?$/
  if (!regex.test(nubmer)) {
    return false
  }
  return true
}

/** 判断输入的是货币 货币的整数部分须少于10位，小数部分须少于2位*/
export function regexIsCRMMoneyNumber(nubmer) {
  var regex = /^([-+]?\d{1,10})(\.\d{0,2})?$/
  if (!regex.test(nubmer)) {
    return false
  }
  return true
}

/** 判断输入的是电话*/
export function regexIsCRMMobile(mobile) {
  var regex = /^(\+?0?\d{2,4}\-?)?\d{6,11}$/
  if (!regex.test(mobile)) {
    return false
  }
  return true
}

/** 判断输入的是邮箱*/
export function regexIsCRMEmail(email) {
  var regex = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/
  if (!regex.test(email)) {
    return false
  }
  return true
}

/**
 * 时间操作
 * @param 
 */
/** 时间戳转date*/
import moment from 'moment'

export function getDateFromTimestamp(time) {
  var times = 0
  if (time.length === 13) {
    times = parseInt(time)
  } else {
    times = parseInt(time) * 1000
  }
  return new Date(times) // 如果date为13位不需要乘1000
}

/**
 * 
 * @param {*} timestamp 时间戳 
 * @param {*} format 格式化
 */
export function timestampToFormatTime(timestamp, format) {
  if (timestamp && timestamp.toString().length >= 10) {
    return moment(getDateFromTimestamp(timestamp.toString())).format(format)
  }
  return ''
}
/**
 * 
 * @param {*} format 格式化字符串
 */
export function formatTimeToTimestamp(format) {
  if (format && format.length > 0) {
    var timeValue = moment(format)
      .valueOf()
      .toString()
    return timeValue.length > 10 ? timeValue.substr(0, 10) : timeValue
  }
  return ''
}

/** image 下载 */
/**
 *
 * @param {*} data url
 * @param {*} filename 名称
 */
export function downloadImage(data, filename) {
  var httpindex = data.indexOf('http')
  if (httpindex === 0) {
    const image = new Image()
    // 解决跨域 canvas 污染问题
    image.setAttribute('crossOrigin', 'anonymous')
    image.onload = function () {
      const canvas = document.createElement('canvas')
      canvas.width = image.width
      canvas.height = image.height
      const context = canvas.getContext('2d')
      context.drawImage(image, 0, 0, image.width, image.height)
      const dataURL = canvas.toDataURL('image/png')
      // 生成一个 a 标签
      const a = document.createElement('a')
      // 创建一个点击事件
      const event = new MouseEvent('click')
      // 将 a 的 download 属性设置为我们想要下载的图片的名称，若 name 不存在则使用'图片'作为默认名称
      a.download = filename || '图片'
      // 将生成的 URL 设置为 a.href 属性
      var blob = dataURLtoBlob(dataURL)
      a.href = URL.createObjectURL(blob)
      // 触发 a 的点击事件
      a.dispatchEvent(event)
    }
    image.src = data
  } else {
    // 生成一个 a 标签
    const a = document.createElement('a')
    // 创建一个点击事件
    const event = new MouseEvent('click')
    // 将 a 的 download 属性设置为我们想要下载的图片的名称，若 name 不存在则使用'图片'作为默认名称
    a.download = filename || '图片'
    // 将生成的 URL 设置为 a.href 属性
    a.href = data
    // 触发 a 的点击事件
    a.dispatchEvent(event)
  }
}
/** 
 * path  和 name
 */
export function downloadFile(data) {
  var a = document.createElement('a')
  a.href = data.path
  a.download = data.name ? data.name : '文件'
  a.target = '_black'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

export function dataURLtoBlob(dataurl) {
  // eslint-disable-next-line one-var
  var arr = dataurl.split(','),
    mime = arr[0].match(/:(.*?);/)[1],
    bstr = atob(arr[1]),
    n = bstr.length,
    u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], {
    type: mime
  })
}

export function getBase64Image(img) {
  var canvas = document.createElement("canvas")
  canvas.width = img.width
  canvas.height = img.height
  var ctx = canvas.getContext("2d")
  ctx.drawImage(img, 0, 0, img.width, img.height)
  var ext = img.src.substring(img.src.lastIndexOf(".") + 1).toLowerCase()
  var dataURL = canvas.toDataURL("image/" + ext)
  return dataURL
}

// 获取绑定参数
export function guid() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  }
  return (S4() + S4() + S4() + S4() + S4() + S4() + S4() + S4());
}
