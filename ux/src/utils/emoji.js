import data from './emoji-data.js'
let emojiData = {}
Object.values(data).forEach(item => {
  emojiData = {
    ...emojiData,
    ...item
  }
})

/**
 *
 *
 * @export
 * @param {string} value
 * @returns {string}
 */

export function emoji(value) {
  if (!value) return
  Object.keys(emojiData).forEach(item => {
    value = value.replace(new RegExp(item, 'g'), createIcon(item))
  })
  return value
}

function createIcon(item) {
  const value = emojiData[item]
  const path = process.env.NODE_ENV == 'development' ? '../../static/img/emoji/' : './static/img/emoji/'
  return `<img src=${path}${value} width="16px" height="16px">`
}
