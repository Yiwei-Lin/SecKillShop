package com.shop.controller;

import com.shop.entity.User;
import com.shop.message.ResultMsg;
import com.shop.redis.RedisService;
import com.shop.redis.prefix.GoodsPrefix;
import com.shop.service.GoodsService;
import com.shop.service.UserService;
import com.shop.vo.GoodsDetailVo;
import com.shop.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;

	@Autowired
	ApplicationContext applicationContext;
/*  已弃用
    添加页面缓存进行优化
        @GetMapping("/list")
        public String list(Model model,User user) {
            model.addAttribute("user", user);
            //查询商品列表
            List<GoodsVo> goodsList = goodsService.listGoodsVo();
            model.addAttribute("goodsList", goodsList);
            return "goods_list";
        }
*/
        @GetMapping(value="/list", produces="text/html")
        public String list(HttpServletRequest request, HttpServletResponse response, Model model,User user) {
            model.addAttribute("user", user);
            // 取缓存
            String html = redisService.get(GoodsPrefix.GOODS_LIST_PREFIX, "list");
            if(!StringUtils.isEmpty(html)) {
                return html;
            }
            List<GoodsVo> goodsList = goodsService.listGoodsVo();
            model.addAttribute("goodsList", goodsList);

            SpringWebContext context = new SpringWebContext(request,response,
                    request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
            // 手动渲染
            html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
            if(!StringUtils.isEmpty(html)) {
                redisService.set(GoodsPrefix.GOODS_LIST_PREFIX, "list", html);
            }
            return html;
        }

        @GetMapping("/detail/{goodsId}")
        public ResultMsg<GoodsDetailVo> detail(Model model, User user,
                                               @PathVariable("goodsId")long goodsId) {
            model.addAttribute("user", user);

            GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
            model.addAttribute("goods", goods);

            long startAt = goods.getStartDate().getTime();
            long endAt = goods.getEndDate().getTime();
            long now = System.currentTimeMillis();

            int seckillStatus,remainSeconds;
            if(now < startAt ) {                                    //秒杀未开始
                seckillStatus = 0;
                remainSeconds = (int)((startAt - now )/1000);
            }else  if(now > endAt || goods.getStockCount() <= 0){   //秒杀已结束
                seckillStatus = 2;
                remainSeconds = -1;
            }else {                                                 //秒杀进行中
                seckillStatus = 1;
                remainSeconds = 0;
            }
            GoodsDetailVo vo = new GoodsDetailVo(goods, user, remainSeconds, seckillStatus);
            return ResultMsg.success(vo);
        }
}
