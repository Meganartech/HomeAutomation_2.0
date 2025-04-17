import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedOutNavbar from "../LoggedOutNavbar";
import Swal from "sweetalert2";

export default function Register() {
    const [formData, setFormData] = useState({
        name: "",
        username: "",
        password: "",
        confirmPassword: "",
        email: "",
        confirmEmail: "",
        roles: ["USER"]
    });
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.password !== formData.confirmPassword) {
            setError("Password does not match !");
            Swal.fire({
                icon: 'error',
                title: 'Register Failed',
                text: error,
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            return;
        }
        if (formData.email !== formData.confirmEmail) {
            setError("Email does not match !");
            Swal.fire({
                icon: 'error',
                title: 'Register Failed',
                text: error,
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            return;
        }
        try {
            const response = await axios.post("http://localhost:8081/user/register", formData,
                { headers: { "Content-Type": "application/json" } }
            );
            if (response.status === 200) {

                await Swal.fire({
                    icon: 'success',
                    text: response.data.message,
                    showConfirmButton: false,
                    timer: 1500,
                    customClass: {
                        popup: 'my-swal-popup',
                        title: 'my-swal-title',
                        htmlContainer: 'my-swal-content',
                        confirmButton: 'my-swal-confirm-button',
                    }
                });
                navigate("/user/login");
            }
            console.log("Register success");
        } catch (err) {
            setError();

            Swal.fire({
                icon: 'error',
                title: 'Register Failed',
                text: err.response?.data.error || "Register failed",
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });

            console.error("Register error: ", err.response?.data.error);
        }
    };

    return (

        <>
            {/* Navbar */}
            <LoggedOutNavbar />

            {/* Registration form */}
            <div className="container mt-12 mb-4">
                <div className="row justify-content-center">
                    <div className="col-xl-8 col-12">
                        <div className="card shadow-lg border-0">
                            <div className="card-body">
                                <h4 className="card-title text-center text-123458">REGISTRATION</h4>
                                <p className="text-danger">* Required field</p>

                                <form onSubmit={handleSubmit}>
                                    <div className="mb-3">
                                        <label className="form-label text-123458" htmlFor="name">Name <span className="text-danger">*</span></label>
                                        <input type="text" id="name" name="name" className="form-control" onChange={handleChange} required />
                                    </div>

                                    <div className="mb-3">
                                        <label className="form-label text-123458" htmlFor="username">Username <span className="text-danger">*</span></label>
                                        <input type="text" id="username" name="username" className="form-control" onChange={handleChange} required />
                                    </div>

                                    <div className="row">
                                        <div className="col-12 col-sm-6 mb-3">
                                            <label className="form-label text-123458" htmlFor="password">Password <span className="text-danger">*</span></label>
                                            <input type="password" id="password" name="password" className="form-control" onChange={handleChange} required />
                                        </div>
                                        <div className="col-12 col-sm-6 mb-3">
                                            <label className="form-label text-123458" htmlFor="confirmPassword">Confirm Password <span className="text-danger">*</span></label>
                                            <input type="password" id="confirmPassword" name="confirmPassword" className="form-control" onChange={handleChange} required />
                                        </div>
                                    </div>

                                    <div className="row">
                                        <div className="col-12 col-sm-6 mb-3">
                                            <label className="form-label text-123458" htmlFor="email">Email Address <span className="text-danger">*</span></label>
                                            <input type="email" id="email" name="email" className="form-control" onChange={handleChange} required />
                                        </div>
                                        <div className="col-12 col-sm-6 mb-3">
                                            <label className="form-label text-123458" htmlFor="confirmEmail">Confirm Email Address <span className="text-danger">*</span></label>
                                            <input type="email" id="confirmEmail" name="confirmEmail" className="form-control" onChange={handleChange} required />
                                        </div>
                                    </div>

                                    <div className="mb-3">
                                        <label className="form-label text-123458" htmlFor="roles">Role <span className="text-danger">*</span></label>
                                        <select id="roles" name="roles" className="form-select" onChange={handleChange} required>
                                            <option value={formData.roles}>User</option>
                                        </select>
                                    </div>

                                    {/* Submit Button */}
                                    <div className="text-center">
                                        <button type="submit" className="btn btn-123458 w-100">Submit</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}