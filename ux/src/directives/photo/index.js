import Vue from 'vue'
import Photo from './photo.vue'
import {
  addClass,
  removeClass,
  getStyle
} from 'element-ui/src/utils/dom'
const Mask = Vue.extend(Photo)

const loadingDirective = {}
loadingDirective.install = Vue => {
  if (Vue.prototype.$isServer) return
  const togglePhoto = (el, binding) => {
    /** 如果是数组 判断数组长度  否则 判断是否存在 当做Boolean */
    if (binding.value && !binding.value.img) {
      Vue.nextTick(() => {
        el.originalPosition = getStyle(el, 'position')
        insertDom(el, el, binding)
      })
    } else { // 移除效果
      el.domVisible = false
      removeClass(el, 'xs-photo-parent--relative')
      removeClass(el, 'xs-photo-parent--hidden')
      el.instance.visible = false
    }
  }
  const insertDom = (parent, el, binding) => {
    if (!el.domVisible && getStyle(el, 'display') !== 'none' && getStyle(el, 'visibility') !== 'hidden') {
      Object.keys(el.maskStyle).forEach(property => {
        el.mask.style[property] = el.maskStyle[property]
      })

      if (el.originalPosition !== 'absolute' && el.originalPosition !== 'fixed') {
        addClass(parent, 'xs-photo-parent--relative')
      }
      el.domVisible = true

      parent.appendChild(el.mask)
      Vue.nextTick(() => {
        el.instance.visible = true
      })
      el.domInserted = true
    }
  }

  Vue.directive('photo', {
    bind: function(el, binding, vnode) {
      const vm = vnode.context
      let text = ''
      if (binding.value && !binding.value.img) {
        text = binding.value.realname && binding.value.realname.length > 2 ? binding.value.realname.substring(binding.value.realname.length - 2, binding.value.realname.length) : binding.value.realname
      }
      const mask = new Mask({
        el: document.createElement('div'),
        data: {
          text: vm && vm[text] || text
        }
      })
      el.instance = mask
      el.mask = mask.$el
      el.maskStyle = {}
      text && togglePhoto(el, binding)
    },

    update: function(el, binding) {
      let text = ''
      if (binding.value && !binding.value.img) {
        text = binding.value.realname && binding.value.realname.length > 2 ? binding.value.realname.substring(binding.value.realname.length - 2, binding.value.realname.length) : binding.value.realname
      }
      el.instance.setText(text)
      if (binding.oldValue !== binding.value) {
        togglePhoto(el, binding)
      }
    },

    unbind: function(el, binding) {
      if (el.domInserted) {
        el.mask &&
          el.mask.parentNode &&
          el.mask.parentNode.removeChild(el.mask)
        togglePhoto(el, {
          value: false,
          modifiers: binding.modifiers
        })
      }
    }
  })
}

export default loadingDirective
