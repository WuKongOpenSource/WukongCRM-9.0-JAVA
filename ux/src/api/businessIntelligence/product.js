import request from '@/utils/request'

/**
 * 产品分类销量分析
 */
export function biProductCategoryAPI(data) {
  return request({
    url: 'biRanking/contractProductRanKing',
    method: 'post',
    data: data
  })
}
