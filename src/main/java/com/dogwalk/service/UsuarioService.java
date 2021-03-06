package com.dogwalk.service;

import java.util.Objects;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dogwalk.dto.MensajeDto;
import com.dogwalk.dto.UsuarioDto;
import com.dogwalk.entity.UsuarioEntity;
import com.dogwalk.repository.UsuarioRepository;
import com.dogwalk.util.Constantes;

@Service
public class UsuarioService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	UsuarioRepository usuarioRepository;

	@Autowired
	UtilService utilService;

	@Autowired
	CorreoService correoService;

	@Autowired
	ModelMapper modelMapper;

	public UsuarioDto crearUsuario(UsuarioEntity usuarioEntity) {

		String nombreMetodo = "crearUsuario";
		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_INICIO);

		UsuarioDto usuarioDto = new UsuarioDto();
		String contrasenaEncriptada = "";
		String mensaje = Constantes.CREACION_USUARIO_FALLIDO;

		contrasenaEncriptada = utilService.encriptarConBase64(usuarioEntity.getContrasena().trim());
		usuarioEntity.setContrasena(contrasenaEncriptada);
		usuarioEntity.setCambiarContrasena(Constantes.ESTADO_INACTIVO);

		UsuarioEntity nuevoUsuario = usuarioRepository.save(usuarioEntity);

		if (nuevoUsuario != null && nuevoUsuario.getId() > 0) {

			usuarioDto = modelMapper.map(nuevoUsuario, UsuarioDto.class);
			mensaje = Constantes.CREACION_USUARIO_EXITOSO;

			logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);

		} else {
			logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);
		}

		usuarioDto.setMensaje(mensaje);

		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_FIN);

		return usuarioDto;
	}

	public UsuarioDto login(String usuario, String contrasena) {

		String nombreMetodo = "login";
		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_INICIO);

		UsuarioDto loginDto = new UsuarioDto();
		String contrasenaEncriptada = "";
		String mensaje = Constantes.LOGIN_FALLIDO;
		boolean datosValidos = Stream.of(usuario, contrasena).allMatch(Objects::nonNull);

		if (datosValidos) {

			usuario = usuario.toUpperCase().trim();
			contrasena = contrasena.trim();
			contrasenaEncriptada = utilService.encriptarConBase64(contrasena);

			UsuarioEntity usuarioEntity = usuarioRepository.findByNombreUsuarioAndContrasena(usuario,
					contrasenaEncriptada);

			if (usuarioEntity != null && usuarioEntity.getId() > 0) {

				loginDto = modelMapper.map(usuarioEntity, UsuarioDto.class);
				mensaje = Constantes.LOGIN_EXITOSO;
				logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);

			} else {
				logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);
			}

		}

		loginDto.setMensaje(mensaje);

		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_FIN);

		return loginDto;
	}

	public MensajeDto cambiarContrasena(Integer idUsuario, String contrasenaActual, String contrasenaNueva) {

		String nombreMetodo = "cambiarContrasena";
		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_INICIO);

		String contrasenaActualEncriptada = "";
		String contrasenaEncriptada = "";
		MensajeDto mensajeDto = new MensajeDto();
		String mensaje = Constantes.CAMBIO_CONTRASENA_FALLIDO;
		boolean datosValidos = Stream.of(idUsuario, contrasenaActual, contrasenaNueva).allMatch(Objects::nonNull);

		if (datosValidos) {

			UsuarioEntity usuarioEntity = usuarioRepository.findUsuarioById(idUsuario);

			if (usuarioEntity != null && usuarioEntity.getId() > 0) {

				contrasenaActualEncriptada = utilService.encriptarConBase64(contrasenaActual.trim());

				if (usuarioEntity.getContrasena().equals(contrasenaActualEncriptada)) {

					contrasenaEncriptada = utilService.encriptarConBase64(contrasenaNueva.trim());
					usuarioEntity.setContrasena(contrasenaEncriptada);
					usuarioEntity.setCambiarContrasena(Constantes.ESTADO_INACTIVO);
					usuarioRepository.save(usuarioEntity);

					mensaje = Constantes.CAMBIO_CONTRASENA_EXITOSO;

					logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);

				} else {
					logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);
				}

			}

		}

		mensajeDto.setMensaje(mensaje);

		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_FIN);

		return mensajeDto;
	}

	public MensajeDto recuperarContrasena(String correo) {

		String nombreMetodo = "recuperarContrasena";
		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_INICIO);

		String contrasenaAutogenerada = "";
		String contrasenaEncriptada = "";
		String mensaje = Constantes.ENVIO_CONTRASENA_FALLIDO;

		boolean envioCorrecto = false;

		MensajeDto mensajeDto = new MensajeDto();

		UsuarioEntity usuarioEntity = usuarioRepository.findByNombreUsuario(correo);

		if (usuarioEntity != null && usuarioEntity.getId() > 0) {

			contrasenaAutogenerada = utilService.generarContrasena(Constantes.LONGITUD_CONTRASENA);
			contrasenaEncriptada = utilService.encriptarConBase64(contrasenaAutogenerada);

			usuarioEntity.setContrasena(contrasenaEncriptada);
			usuarioEntity.setCambiarContrasena(Constantes.ESTADO_ACTIVO);

			envioCorrecto = correoService.enviarCorreo(correo, contrasenaAutogenerada);

			if (envioCorrecto) {
				usuarioRepository.save(usuarioEntity);
				mensaje = Constantes.ENVIO_CONTRASENA_EXITOSO;
				logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);
			} else {
				logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, mensaje);
			}

		}

		mensajeDto.setMensaje(mensaje);

		logger.info(Constantes.LOG_FORMATO, Constantes.LOG_SERVICE_USUARIO, nombreMetodo, Constantes.LOG_METODO_FIN);

		return mensajeDto;
	}

}
