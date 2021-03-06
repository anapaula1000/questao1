package br.com.campanha.webhook.api.rest;

import br.com.campanha.api.domain.ErrorInfo;
import br.com.campanha.webhook.api.domain.WebhookResource;
import br.com.campanha.webhook.domain.Webhook;
import br.com.campanha.webhook.exception.WebhookCadastradoException;
import br.com.campanha.webhook.service.WebhookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

/**
 *  @author : Ana Paula  anapaulasilva1000@gmail.com
 */
@RestController
@RequestMapping("/v1/webhooks")
@Api(value = "Webhook", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
        tags = {"Endpoint de Webhook"}, description = "Lida com todas as requis��es para o servi�o Rest de Webhook",
        basePath = "/api/v1/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(WebhookCadastradoException.class)
    @ResponseBody ErrorInfo
    handleSocioTorcedorJaCadastradoException( WebhookCadastradoException ex) {
        return new ErrorInfo(ServletUriComponentsBuilder.fromCurrentRequest().path("").toUriString() ,ex);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ErrorInfo handleInternalServerError(Exception ex) {
        return new ErrorInfo(ServletUriComponentsBuilder.fromCurrentRequest().path("").toUriString() , ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody ErrorInfo
    handleHttpMessageNotReadableException( HttpMessageNotReadableException ex) {
        return new ErrorInfo(ServletUriComponentsBuilder.fromCurrentRequest().path("").toUriString() ,ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody ErrorInfo
    handleValidationException( MethodArgumentNotValidException ex) {
        return new ErrorInfo(ServletUriComponentsBuilder.fromCurrentRequest().path("").toUriString() ,ex);
    }


    @Autowired
    private WebhookService webhookService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Cria um novo webhook com os parametros recebidos",
            notes = "Cria uma novo webhook para ser usado nas notifica��es e retorna um novo link " +
                    "webhook  criada",
            response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400 , message = "Bad Request"),
            @ApiResponse(code = 500 , message = "Internal Server Error")})
    public ResponseEntity<?> cadastrarWebhook(@Valid @RequestBody WebhookResource webhookResource){

        try {
            final Webhook webhook =
                    webhookService.cadastrarWebhook(webhookResource.getUrl(), webhookResource.getChaveAcesso());

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{id}")
                    .buildAndExpand(webhook.getId()).toUri();

            if (logger.isDebugEnabled()) {
                logger.debug("Webhook : {} criado com sucesso", webhook);
                logger.debug("O link gerado foi : {}", location);
            }
            return created(location).build();

        }catch (DuplicateKeyException ex){
            if(logger.isDebugEnabled()){
                logger.debug("Webhook com url: {} ja cadastrado", webhookResource.getUrl());
            }
            throw new WebhookCadastradoException();
        }
    }
}
