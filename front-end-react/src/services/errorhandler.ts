import axios from "axios";

export function getErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    if (error.response?.data?.message) {
      return error.response.data.message;
    } else {
      switch (error.response?.status) {
        case 401:
          return "Unauthorized";
        case 400:
          return "Dados inválidos";
        case 404:
          return "Recurso não encontrado";
        case 500:
          return "Erro no servidor";
        default:
          return "Erro na requisição";
      }
    }
  }

  return "Erro inesperado";
}
